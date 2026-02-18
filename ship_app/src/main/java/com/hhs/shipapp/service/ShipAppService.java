package com.hhs.shipapp.service;

import com.hhs.lib.model.*;
import com.hhs.shipapp.connection.ShipClientConnection;
import com.hhs.shipapp.connection.ShipConnectionManager;
import com.hhs.shipapp.models.RadarResponse;
import com.hhs.shipapp.models.ShipEntityState;
import com.hhs.shipapp.models.ShipMessage;
import com.hhs.shipapp.models.enums.Commands;
import com.hhs.shipapp.models.enums.Course;
import com.hhs.shipapp.models.enums.Rudder;
import com.hhs.shipapp.util.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShipAppService {

  private final ShipConnectionManager shipConnectionManager;
  private final ShipAppImpl shipAppImpl;
  private final ShipTransportMessage shipTransportMessage;
  private final ShipAppComponent shipAppComponent;
  private final Set<String> checkedShipIds = new HashSet<>();

  private final int delayMillis = 100;
  private WallFollowMode wallFollowMode = WallFollowMode.LEFT;
  private final Map<String, Boolean> forwardCenterBlocked = new HashMap<>();
  private final Map<String, Integer> stepStateCounter = new HashMap<>();
  private final Map<String, Boolean> shipWasOnLaunchPad = new HashMap<>();

  private static final Logger log = LoggerFactory.getLogger(ShipAppService.class);

  public ShipAppService(ShipAppImpl shipAppImpl, ShipTransportMessage shipTransportMessage, Map<String, ShipEntityState> shipEntityStateMap,
      ShipConnectionManager shipConnectionManager) {
    this.shipAppImpl = shipAppImpl;
    this.shipTransportMessage = shipTransportMessage;
    this.shipAppComponent = new ShipAppComponent(this.shipTransportMessage, shipEntityStateMap);
    this.shipConnectionManager = shipConnectionManager;
  }

  /**
   * Startet ein neues Schiff im angegebenen Sektor mit Richtung.
   *
   * @return shipId bei Erfolg oder wirft eine Exception bei Fehlern
   */
  public String launchShip(String name, int x, int y, int dx, int dy) {
    if (x < 0 || x > 99 || y < 0 || y > 99) {
      return "Sector liegt ausserhalb von Forschungsgebiet: " + "(" + x + ", " + y + ")";
    }

    if (dx < -1 || dx > 1 || dy < -1 || dy > 1) {
      return "unerwartete Richtungseingabe: " + "(" + dx + ", " + dy + ")";
    }

    Vec2D sector = new Vec2D(x, y);
    Vec2D direction = new Vec2D(dx, dy);

    if (!shipTransportMessage.isSectorFree(sector)) {
      return "Sector is not free: " + sector;
    }

    // Schiff landen
    List<ShipMessage> shipMessages = shipAppImpl.launch(name, sector, direction);
    ShipMessage firstMessage = shipMessages.getFirst();

    String shipId = firstMessage.getId();

    if (firstMessage.getCmd() != Commands.launched) {
      return "Ship launch failed: " + firstMessage.getCmd();
    }

    shipAppComponent.setShipId(shipId);
    shipAppComponent.setShipSector(sector);
    shipAppComponent.setShipDirection(direction);
    shipAppComponent.setShipMessages(shipMessages);

    // ShipEntityState befüllen
    shipAppComponent.fillShipEntityStateMap();

    // Daten persistieren
    saveSectorAndShipData(shipId, name, x, y, dx, dy);

    // ShipSector in DB persistieren
    shipAppComponent.saveShipSector();

    return shipId;
  }

  private void saveSectorAndShipData(String shipId, String name, int x, int y, int dx, int dy) {
    SectorData sectorData = new SectorData(shipId, x, y);
    sectorData.setGround(Ground.Harbour);
    shipTransportMessage.saveSectorData(sectorData);

    Ship ship = new Ship(shipId, name, x, y, dx, dy);
    shipTransportMessage.saveShipData(ship);
  }

  /**
   * Führt einen Radar-Scan für das angegebene Schiff durch.
   *
   * @param shipId Die ID des Schiffs
   * @return RadarResponse mit den gescannten Informationen
   * @throws IllegalArgumentException wenn das Schiff nicht existiert
   * @throws IllegalStateException    bei Verbindungs- oder Ausführungsproblemen
   */
  public RadarResponse radar(String shipId) {
    // aktualisieren shipId
    shipAppComponent.setShipId(shipId);

    // ShipId-Prüfung für eine shipId nur einmal ausführen, um die Rechenzeit zu sparen
    if (!checkedShipIds.contains(shipId)) {
      if (!shipTransportMessage.existsShipIdInDB(shipId)) {
        throw new IllegalArgumentException("Ship with ID " + shipId + " does not exist in database");
      }

      checkedShipIds.add(shipId);
    }

    // Radar-Befehl an Server senden
    ShipClientConnection clientConnection = shipConnectionManager.get(shipId);

    List<ShipMessage> shipMessages = shipAppImpl.radar(shipId);
    if (shipMessages == null || shipMessages.isEmpty()) {
      throw new IllegalStateException("No response received from radar command");
    }

    // shipMessage in shipAppComponent aktuallisieren
    shipAppComponent.setShipMessages(shipMessages);

    // Radar-Response erstellen
    RadarResponse radarResponse = shipAppComponent.getRadarResponse();

    // Gefundene Sektoren persistent speichern
    shipAppComponent.persistSectorData();

    return radarResponse;
  }

  /**
   * Führt einen Scan-Befehl für das angegebene Schiff aus und gibt die gemessene Tiefe zurück.
   *
   * @param shipId Die ID des Schiffs
   * @return die gemessene Tiefe (als Integer)
   * @throws IllegalArgumentException wenn das Schiff nicht existiert
   * @throws IllegalStateException    bei Verbindungsproblemen oder ungültiger Antwort
   */
  public ScanResponse scan(String shipId) {
    // aktualisieren shipId
    shipAppComponent.setShipId(shipId);

    // ShipId-Prüfung für eine shipId nur einmal ausführen, um die Rechenzeit zu sparen
    if (!checkedShipIds.contains(shipId)) {
      if (!shipTransportMessage.existsShipIdInDB(shipId)) {
        throw new IllegalArgumentException("Ship with ID " + shipId + " does not exist in database");
      }

      checkedShipIds.add(shipId);
    }

    // Scan-Befehl an den Server senden
    List<ShipMessage> shipMessages = shipAppImpl.scan(shipId);
    if (shipMessages == null || shipMessages.isEmpty()) {
      throw new IllegalStateException("No response received from scan command");
    }

    ShipMessage scanMessage = shipMessages.getFirst();
    shipAppComponent.setShipMessages(shipMessages);

    // Sector-Daten aktualisieren
    shipAppComponent.updateSectorData();

    return new ScanResponse(scanMessage.getDepth(), scanMessage.getStddev());
  }

  /**
   * Führt eine Navigation für das angegebene Schiff aus.
   *
   * @param shipId Die ID des Schiffs
   * @param course Der gewünschte Kurs (z. B. "left", "right", "straight", ...)
   * @param rudder Der gewünschte Rudereinschlag
   * @return true wenn die Navigation erfolgreich war, false bei Crash oder Fehler
   * @throws IllegalArgumentException bei ungültigem shipId oder fehlendem State
   * @throws IllegalStateException    bei Verbindungsproblemen
   */
  public NavigateResponse navigate(String shipId, String course, String rudder) {
    // aktualisieren shipId
    shipAppComponent.setShipId(shipId);

    // ShipId-Prüfung für eine shipId nur einmal ausführen, um die Rechenzeit zu sparen
    if (!checkedShipIds.contains(shipId)) {
      if (!shipTransportMessage.existsShipIdInDB(shipId)) {
        throw new IllegalArgumentException("Ship with ID " + shipId + " does not exist in database");
      }

      checkedShipIds.add(shipId);
    }

    // Navigation ausführen
    List<ShipMessage> shipMessages = shipAppImpl.navigate(shipId, course, rudder);
    if (shipMessages == null || shipMessages.isEmpty()) {
      throw new IllegalStateException("No response received from navigate command");
    }

    boolean success = shipMessages.getFirst().getCmd() != Commands.crash;

    if (success) {
      shipAppComponent.setShipMessages(shipMessages);

      // ship sector, direction und relativeCoordinateSystem aktualisieren
      shipAppComponent.updateShipSectorAndDirectionAndCoordinateSystem();

      // State aktualisieren
      shipAppComponent.updateShipEntityState(course, rudder);

      // ShipData in DB aktualisieren
      Ship updatedShip = shipAppComponent.fetchUpdatedShipData();
      shipTransportMessage.updateShipData(updatedShip);

      // ShipSector in DB persistieren
      shipAppComponent.saveShipSector();

      return new NavigateResponse(shipAppComponent.getShipDirection().getX(), shipAppComponent.getShipDirection().getY());
    } else {
      // Bei Crash: Verbindung beenden
      exit(shipId);

      return null;
    }
  }

  /**
   * Beendet die Verbindung eines Schiffs zum Server und entfernt die zugehörigen Daten.
   *
   * @param shipId Die ID des Schiffs, das beendet werden soll
   * @return true, wenn der Exit-Befehl erfolgreich gesendet und die Verbindung geschlossen wurde
   * @throws IllegalArgumentException wenn das Schiff nicht existiert
   * @throws IllegalStateException    bei Verbindungsproblemen
   */
  public boolean exit(String shipId) {
    // ShipId-Prüfung für eine shipId nur einmal ausführen, um die Rechenzeit zu sparen
    if (!checkedShipIds.contains(shipId)) {
      if (!shipTransportMessage.existsShipIdInDB(shipId)) {
        throw new IllegalArgumentException("Ship with ID " + shipId + " does not exist in database");
      }

      checkedShipIds.add(shipId);
    }

    // Schiff-Daten aus der Datenbank entfernen
    shipTransportMessage.removeShipData(shipId);

    // Exit-Befehl an den Server senden und Verbindung schließen
    boolean success = shipAppImpl.exit(shipId);

    // entfernen von shipId von checkedShipIds
    checkedShipIds.remove(shipId);

    // shipEntityStateMap bereinigen
    shipAppComponent.removeShipEntityState(shipId);

    return success;
  }

  // ===== Autopilot =====

  /**
   * Führt einen Autopilot-Schritt für das angegebene Schiff aus.
   * Scannt, analysiert Umgebung, plant nächste Bewegung.
   *
   * @param shipId Die ID des Schiffs
   * @return AutoPilotData mit Ergebnissen des Schritts (aktuell noch minimal)
   * @throws IllegalArgumentException bei ungültigem Schiff oder fehlendem Zustand
   * @throws IllegalStateException    bei Verbindungs- oder Ausführungsproblemen
   */
  public AutoPilotData runAutoPilot(String shipId) {

    shipAppComponent.setShipId(shipId);

    boolean wasAlreadyOnLaunchPad = shipWasOnLaunchPad.getOrDefault(shipId, false);

    // wurde ein ship bereits zum Startposition gebracht, dann nicht noch mal zum Startposition bringen
    if (!wasAlreadyOnLaunchPad) {
      bringShipToLaunchPad(shipId);
    }

    refreshShip(shipId);

    if (shipAppComponent.isShipBlocked()) {
      handleCollisionWhileMoving_North(shipId);

      return shipAppComponent.buildAutoPilotData();
    } else {
      shipAppComponent.setShipGoalDirection(ShipGoalDirection.NORTH.getKey());

      startFollowWall(shipId);

      return shipAppComponent.buildAutoPilotData();
    }

  }

  private void handleCollisionWhileMoving_North(String shipId) {
    do {
      Helper.sleepForMillis(delayMillis);
      navigate(shipId, Course.Backward.getKey(), Rudder.Center.getKey());


      startFollowWall(shipId);
    } while (shipAppComponent.isShipBlocked());
  }

  private void bringShipToLaunchPad(String shipId) {
    shipWasOnLaunchPad.put(shipId, false);

    shipAppComponent.setShipGoalDirection(ShipGoalDirection.NORTH.getKey());

    shipAppComponent.setDriveableShipGoalDirection(ShipStraightOnDirection.Disabled);

    boolean shipReachedWestOrSouthSeaBoundary = moveShipTo_SouthOrWestSeaBoundary(shipId);

    if (shipReachedWestOrSouthSeaBoundary) {

      if (shipAppComponent.isShipAtWestBoundary()) {
        moveShipTo_SouthSeaBoundary(shipId);
      }

      if (shipAppComponent.isShipAtSouthBoundary()) {
        boolean reachedWest = moveShipTo_WestSeaBoundary(shipId);

        if (reachedWest) {
          bringShipToStartPosition(shipId);
        }
      }
    }
  }

  // Autopilot - to southWest
  private boolean moveShipTo_SouthOrWestSeaBoundary(String shipId) {
    shipAppComponent.setShipGoalDirection(ShipGoalDirection.SOUTH_WEST.getKey());

    refreshShip(shipId);

    while (!shipAppComponent.isShipAtWestBoundary() && !shipAppComponent.isShipAtSouthBoundary()) {
      refreshShip(shipId);

      if (driveableTo_SouthWestSeaBoundary()) {
        if (shipAppComponent.getDriveableShipGoalDirection().equals(ShipStraightOnDirection.Forward)) {
          if (shipAppComponent.isShipBlocked()) {
            handleCollisionWhileMoving_SouthWest(shipId);
            continue;
          }

          recoverFromCirculationMovement(shipId);

          if (shipAppComponent.driveableWithCommand(Course.Forward.getKey(), Rudder.Center.getKey())) {
            navigate(shipId, Course.Forward.getKey(), Rudder.Center.getKey());
            continue;
          }
        }
      }

      startFollowWall(shipId);
    }

    return shipAppComponent.getShipSector().getX() == 0 || shipAppComponent.getShipSector().getY() == 0;
  }

  private void handleCollisionWhileMoving_SouthWest(String shipId) {
    Helper.sleepForMillis(delayMillis);
    navigate(shipId, Course.Backward.getKey(), Rudder.Center.getKey());

    startFollowWall(shipId);

    while (shipAppComponent.isShipBlocked()) {
      Helper.sleepForMillis(delayMillis);
      navigate(shipId, Course.Backward.getKey(), Rudder.Center.getKey());

      startFollowWall(shipId);

      if (driveableTo_SouthWestSeaBoundary()) {
        break;
      }

    }
  }

  private boolean driveableTo_SouthWestSeaBoundary() {
    return shipAppComponent.getDriveableShipGoalDirection() != ShipStraightOnDirection.Disabled && (
        shipAppComponent.getShipSector().getX() != 0 || shipAppComponent.getShipSector().getY() != 0);
  }

  // Autopilot - to west
  private boolean moveShipTo_WestSeaBoundary(String shipId) {
    shipAppComponent.setShipGoalDirection(ShipGoalDirection.WEST.getKey());

    refreshShip(shipId);

    while (!shipAppComponent.isShipAtWestBoundary()) {
      refreshShip(shipId);

      if (driveableTo_SouthSeaBoundary()) {
        if (shipAppComponent.getDriveableShipGoalDirection().equals(ShipStraightOnDirection.Forward)) {
          if (shipAppComponent.isShipBlocked()) {
            handleCollisionWhileMoving_South(shipId);
            continue;
          }

          recoverFromCirculationMovement(shipId);

          if (shipAppComponent.driveableWithCommand(Course.Forward.getKey(), Rudder.Center.getKey())) {
            navigate(shipId, Course.Forward.getKey(), Rudder.Center.getKey());
            continue;
          }
        }
      }

      startFollowWall(shipId);
    }

    return shipAppComponent.getShipSector().getX() == 0;
  }

  private void bringShipToStartPosition(String shipId) {
    System.out.println("will bring ship to start!");

    if (shipAppComponent.isShipAtWestBoundary()) {
      if (4 <= shipAppComponent.getShipSector().getY() && shipAppComponent.getShipSector().getY() <= 8) {
        if (shipAppComponent.getShipDirection().equals(ShipGoalDirection.WEST.getKey())) {
          Helper.sleepForMillis(delayMillis);
          navigate(shipId, Course.Backward.getKey(), Rudder.Left.getKey());

          Helper.sleepForMillis(delayMillis);
          navigate(shipId, Course.Backward.getKey(), Rudder.Left.getKey());

          while (shipAppComponent.getShipSector().getY() > 2) {
            Helper.sleepForMillis(delayMillis);
            navigate(shipId, Course.Backward.getKey(), Rudder.Center.getKey());
          }

          Helper.sleepForMillis(delayMillis);
          navigate(shipId, Course.Backward.getKey(), Rudder.Left.getKey());

          Helper.sleepForMillis(delayMillis);
          navigate(shipId, Course.Backward.getKey(), Rudder.Right.getKey());
        }
      }
    }

    if (shipAppComponent.getShipSector().getX() == 0 && shipAppComponent.getShipSector().getY() == 0) {
      shipWasOnLaunchPad.put(shipId, true);
    }

  }

  private boolean driveableTo_WestSeaBoundary() {
    return shipAppComponent.getDriveableShipGoalDirection() != ShipStraightOnDirection.Disabled
        && shipAppComponent.getShipSector().getX() != 0;
  }

  private void handleCollisionWhileMoving_West(String shipId) {
    Helper.sleepForMillis(delayMillis);
    navigate(shipId, Course.Backward.getKey(), Rudder.Center.getKey());

    startFollowWall(shipId);

    while (shipAppComponent.isShipBlocked()) {
      Helper.sleepForMillis(delayMillis);
      navigate(shipId, Course.Backward.getKey(), Rudder.Center.getKey());

      startFollowWall(shipId);

      if (driveableTo_WestSeaBoundary()) {
        break;
      }

    }
  }

  // Autopilot - to south
  private boolean moveShipTo_SouthSeaBoundary(String shipId) {
    shipAppComponent.setShipGoalDirection(ShipGoalDirection.SOUTH.getKey());

    refreshShip(shipId);

    while (!shipAppComponent.isShipAtSouthBoundary()) {
      refreshShip(shipId);

      if (driveableTo_SouthSeaBoundary()) {
        if (shipAppComponent.getDriveableShipGoalDirection().equals(ShipStraightOnDirection.Forward)) {
          if (shipAppComponent.isShipBlocked()) {
            handleCollisionWhileMoving_South(shipId);
            continue;
          }

          recoverFromCirculationMovement(shipId);

          if (shipAppComponent.driveableWithCommand(Course.Forward.getKey(), Rudder.Center.getKey())) {
            navigate(shipId, Course.Forward.getKey(), Rudder.Center.getKey());
            continue;
          }
        }
      }

      startFollowWall(shipId);
    }

    return shipAppComponent.getShipSector().getY() == 0; // TODO check, if sector at (0,0)
  }

  private void handleCollisionWhileMoving_South(String shipId) {
    do {
      Helper.sleepForMillis(delayMillis);
      if (shipAppComponent.driveableWithCommand(Course.Backward.getKey(), Rudder.Center.getKey())) {
        navigate(shipId, Course.Backward.getKey(), Rudder.Center.getKey());
      }

      forwardCenterBlocked.put(shipId, true);

      followNorth(shipId);
    } while (shipAppComponent.isShipBlocked());
  }

  private boolean driveableTo_SouthSeaBoundary() {
    return shipAppComponent.getDriveableShipGoalDirection() != ShipStraightOnDirection.Disabled
        && shipAppComponent.getShipSector().getY() != 0;
  }

  //  Autopilot - Hilfsmethoden
  private void startFollowWall(String shipId) {
    refreshShip(shipId);

    ShipGoalDirection goalDirection = ShipGoalDirection.fromVec2D(shipAppComponent.getShipGoalDirection());

    Objects.requireNonNull(goalDirection, "goalDirection must not be null");
    if (!shipAppComponent.isShipAtNorthtBoundary()) {
      switch (goalDirection) {
        case NORTH, WEST, SOUTH_WEST -> {
          wallFollowMode = WallFollowMode.LEFT;
          wallFollowLeft(shipId);
        }
        case SOUTH -> {
          wallFollowMode = WallFollowMode.RIGHT;
          wallFollowRight(shipId);
        }
      }
    } else {
      wallFollowMode = WallFollowMode.RIGHT;
      wallFollowRight(shipId);
    }

    refreshShip(shipId);
  }

  private boolean isCirculationMovementDetected(String shipId) {
    String mapKey = shipId + shipAppComponent.getShipSector() + shipAppComponent.getShipDirection() + wallFollowMode.name();

    int count = stepStateCounter.getOrDefault(mapKey, 0) + 1;
    stepStateCounter.put(mapKey, count);

    // sonst wächst die Map unendlich
    if (stepStateCounter.size() > 100) {
      stepStateCounter.clear();
    }

    return count >= 3;
  }

  private void refreshShip(String shipId) {
    scan(shipId);

    RadarResponse radarResponse = radar(shipId);
    shipAppComponent.setRadarResponse(radarResponse);

    shipAppComponent.updateDriveableShipGoalDirection();

    shipAppComponent.calculateNavigableDirections();
  }

  private void handleShipCirculation(String shipId) {
    forwardCenterBlocked.put(shipId, true);

    Helper.sleepForMillis(delayMillis);
    navigate(shipId, Course.Backward.getKey(), Rudder.Center.getKey());
    refreshShip(shipId);

    Helper.sleepForMillis(delayMillis);
    navigate(shipId, Course.Backward.getKey(), Rudder.Center.getKey());
    refreshShip(shipId);

    shipAppComponent.updateDriveableShipGoalDirection();
  }

  private void recoverFromCirculationMovement(String shipId) {
    boolean foundCirculationMovement = isCirculationMovementDetected(shipId);
    if (foundCirculationMovement) {
      if (shipAppComponent.driveableWithCommand(Course.Backward.getKey(), Rudder.Right.getKey())) {
        Helper.sleepForMillis(delayMillis);
        navigate(shipId, Course.Backward.getKey(), Rudder.Right.getKey());
      }
      if (shipAppComponent.driveableWithCommand(Course.Backward.getKey(), Rudder.Left.getKey())) {
        Helper.sleepForMillis(delayMillis);
        navigate(shipId, Course.Backward.getKey(), Rudder.Left.getKey());
      }

      String mapKey = shipId + shipAppComponent.getShipSector() + shipAppComponent.getShipDirection() + wallFollowMode.name();

      log.warn("CirculationMovement: stepStateCounter: " + stepStateCounter.get(mapKey));

      refreshShip(shipId);
    }

  }

  private void tryDriveBackwardCenter(String shipId) {
    if (shipAppComponent.driveableWithCommand(Course.Backward.getKey(), Rudder.Center.getKey())) {
      Helper.sleepForMillis(delayMillis);
      navigate(shipId, Course.Backward.getKey(), Rudder.Center.getKey());
      shipAppComponent.updateDriveableShipGoalDirection();
    }
  }

  private void wallFollowLeft(String shipId) {
    boolean foundCirculation = isCirculationMovementDetected(shipId);

    boolean isForwardCenterBlocked = forwardCenterBlocked.getOrDefault(shipId, false);

    // === SONDERFALL ===, da Schiff ist in Kreisbewegung
    if (foundCirculation) {
      handleShipCirculation(shipId);
      return;
    }

    // === NORMALFALL ===, also bevor das Schiff sich in einer Kreisbewegung gesetzt hat.
    if (shipAppComponent.driveableWithCommand(Course.Forward.getKey(), Rudder.Left.getKey())) {
      forwardCenterBlocked.put(shipId, false);
      Helper.sleepForMillis(delayMillis);
      navigate(shipId, Course.Forward.getKey(), Rudder.Left.getKey());
      shipAppComponent.updateDriveableShipGoalDirection();

      return;
    }

    if ((shipAppComponent.driveableWithCommand(Course.Forward.getKey(), Rudder.Center.getKey()) && !isForwardCenterBlocked)) {
      navigate(shipId, Course.Forward.getKey(), Rudder.Center.getKey());
      shipAppComponent.updateDriveableShipGoalDirection();

      return;
    }

    if (shipAppComponent.driveableWithCommand(Course.Forward.getKey(), Rudder.Right.getKey())) {
      forwardCenterBlocked.put(shipId, false);
      Helper.sleepForMillis(delayMillis);
      navigate(shipId, Course.Forward.getKey(), Rudder.Right.getKey());
      shipAppComponent.updateDriveableShipGoalDirection();

      return;
    }

    tryDriveBackwardCenter(shipId);

    refreshShip(shipId);
  }
  private void followNorth(String shipId) {
    boolean foundCirculation = isCirculationMovementDetected(shipId);

    boolean isForwardCenterBlocked = forwardCenterBlocked.getOrDefault(shipId, false);

    // === SONDERFALL ===, da Schiff ist in Kreisbewegung
    if (foundCirculation) {
      handleShipCirculation(shipId);
      return;
    }

    // === NORMALFALL ===, also bevor das Schiff sich in einer Kreisbewegung gesetzt hat.
    if (shipAppComponent.driveableWithCommand(Course.Forward.getKey(), Rudder.Left.getKey())) {
      forwardCenterBlocked.put(shipId, false);
      Helper.sleepForMillis(delayMillis);
      navigate(shipId, Course.Forward.getKey(), Rudder.Left.getKey());
      shipAppComponent.updateDriveableShipGoalDirection();

      return;
    }

    if ((shipAppComponent.driveableWithCommand(Course.Forward.getKey(), Rudder.Center.getKey()) && !isForwardCenterBlocked)) {
      navigate(shipId, Course.Forward.getKey(), Rudder.Center.getKey());
      shipAppComponent.updateDriveableShipGoalDirection();

      return;
    }

    if (shipAppComponent.driveableWithCommand(Course.Forward.getKey(), Rudder.Right.getKey())) {
      forwardCenterBlocked.put(shipId, false);
      Helper.sleepForMillis(delayMillis);
      navigate(shipId, Course.Forward.getKey(), Rudder.Right.getKey());
      shipAppComponent.updateDriveableShipGoalDirection();

      return;
    }

    tryDriveBackwardCenter(shipId);

    refreshShip(shipId);
  }

  private void wallFollowRight(String shipId) {
    boolean foundCirculation = isCirculationMovementDetected(shipId);

    boolean isForwardCenterBlocked = forwardCenterBlocked.getOrDefault(shipId, false);

    //    shipAppComponent.calculateNavigableDirections();

    // === SONDERFALL ===, da Schiff ist in Kreisbewegung
    if (foundCirculation) {
      handleShipCirculation(shipId);
      return;
    }

    // === NORMALFALL ===, also bevor das Schiff sich in einer Kreisbewegung gesetzt hat.
    if (shipAppComponent.driveableWithCommand(Course.Forward.getKey(), Rudder.Right.getKey())) {
      forwardCenterBlocked.put(shipId, false);

      Helper.sleepForMillis(delayMillis);
      navigate(shipId, Course.Forward.getKey(), Rudder.Right.getKey());
      shipAppComponent.updateDriveableShipGoalDirection();
      return;
    }

    if ((shipAppComponent.driveableWithCommand(Course.Forward.getKey(), Rudder.Center.getKey())) && !isForwardCenterBlocked) {
      navigate(shipId, Course.Forward.getKey(), Rudder.Center.getKey());
      shipAppComponent.updateDriveableShipGoalDirection();
      return;
    }

    if (shipAppComponent.driveableWithCommand(Course.Forward.getKey(), Rudder.Left.getKey())) {
      forwardCenterBlocked.put(shipId, false);

      Helper.sleepForMillis(delayMillis);
      navigate(shipId, Course.Forward.getKey(), Rudder.Left.getKey());
      shipAppComponent.updateDriveableShipGoalDirection();
      return;
    }

    tryDriveBackwardCenter(shipId);

    refreshShip(shipId);
  }

}