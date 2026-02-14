package com.hhs.shipapp.service;

import com.hhs.lib.model.*;
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

  private final ShipAppImpl shipAppImpl;
  private final ShipTransportMessage shipTransportMessage;
  private final ShipAppComponent shipAppComponent;
  private final Set<String> checkedShipIds = new HashSet<>();

  private final int delayMillis = 500;
  private WallFollowMode wallFollowMode;
  private final Map<String, Boolean> forwardCenterBlocked = new HashMap<>();
  private final Map<String, Integer> stepStateCounter = new HashMap<>();


  private static final Logger log = LoggerFactory.getLogger(ShipAppService.class);

  public ShipAppService(ShipAppImpl shipAppImpl, ShipTransportMessage shipTransportMessage,
                        Map<String, ShipEntityState> shipEntityStateMap) {
    this.shipAppImpl = shipAppImpl;
    this.shipTransportMessage = shipTransportMessage;
    this.shipAppComponent = new ShipAppComponent(this.shipTransportMessage, shipEntityStateMap);
  }

  private void ensureConnectedToShipServer() {
    if (!shipAppImpl.getConnectionState()) {
      shipAppImpl.connectShipClientToShipServer();

      if (!shipAppImpl.getConnectionState()) {
        log.info("connection to ship server failed");
      }
    }
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

    ensureConnectedToShipServer();

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
    ensureConnectedToShipServer();

    // ShipId-Prüfung für eine shipId nur einmal ausführen, um die Rechenzeit zu sparen
    if (!checkedShipIds.contains(shipId)) {
      if (!shipTransportMessage.existsShipIdInDB(shipId)) {
        throw new IllegalArgumentException("Ship with ID " + shipId + " does not exist in database");
      }

      checkedShipIds.add(shipId);
    }

    // Radar-Befehl an Server senden
    List<ShipMessage> shipMessages = shipAppImpl.radar();
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
    ensureConnectedToShipServer();

    // ShipId-Prüfung für eine shipId nur einmal ausführen, um die Rechenzeit zu sparen
    if (!checkedShipIds.contains(shipId)) {
      if (!shipTransportMessage.existsShipIdInDB(shipId)) {
        throw new IllegalArgumentException("Ship with ID " + shipId + " does not exist in database");
      }

      checkedShipIds.add(shipId);
    }

    // Scan-Befehl an den Server senden
    List<ShipMessage> shipMessages = shipAppImpl.scan();
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
    ensureConnectedToShipServer();

    // ShipId-Prüfung für eine shipId nur einmal ausführen, um die Rechenzeit zu sparen
    if (!checkedShipIds.contains(shipId)) {
      if (!shipTransportMessage.existsShipIdInDB(shipId)) {
        throw new IllegalArgumentException("Ship with ID " + shipId + " does not exist in database");
      }

      checkedShipIds.add(shipId);
    }

    // Navigation ausführen
    List<ShipMessage> shipMessages = shipAppImpl.navigate(course, rudder);
    if (shipMessages == null || shipMessages.isEmpty()) {
      throw new IllegalStateException("No response received from navigate command");
    }

    boolean success = shipMessages.getFirst().getCmd() != Commands.crash;

    if (success) {
      shipAppComponent.setShipMessages(shipMessages);

      // ship sector und direction aktualisieren
      shipAppComponent.updateShipSectorAndDirection();

      // State aktualisieren
      shipAppComponent.updateShipEntityState(course, rudder);

      // ShipData in DB aktualisieren
      Ship updatedShip = shipAppComponent.fetchUpdatedShipData();
      shipTransportMessage.updateShipData(updatedShip);

      // ShipSector in DB persistieren
      shipAppComponent.saveShipSector();

      return new NavigateResponse(shipAppComponent.getShipDirection().getX(),
                                  shipAppComponent.getShipDirection().getY());
    }
    else {
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
    ensureConnectedToShipServer();

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
    boolean success = shipAppImpl.exit();

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
    AutoPilotData result = new AutoPilotData();

    shipAppComponent.setShipId(shipId);
    shipAppComponent.setShipGoalDirection(ShipGoalDirection.NORTH.getKey());
    shipAppComponent.setDriveableToShipGoalDirection(ShipStraightOnDirection.Disabled);

    boolean hasShipReached_WestSeaBoundary = moveShipToWestSeaBoundary(shipId);

    if (hasShipReached_WestSeaBoundary) {
      boolean hasShipReached_AutopilotPosition = moveShipToAutopilotPosition(shipId);

      if (hasShipReached_AutopilotPosition) {
        System.out.println("AutoPilot: Move ship to autopilot position succeed: " + hasShipReached_AutopilotPosition);
      }

    }

    return result;
  }

  // ===== Autopilot - to south
  private boolean moveShipToAutopilotPosition(String shipId) {
    shipAppComponent.setShipGoalDirection(ShipGoalDirection.SOUTH.getKey());

    Vec2D south = ShipGoalDirection.SOUTH.getKey();
    shipAppComponent.setShipGoalDirection(south);

    refreshScanAndRadar(shipId);

    while (!shipAppComponent.isShipAtSouthBoundary()) {
      if (driveableTo_SouthSeaBoundary()) {
        if (shipAppComponent.getDriveableToShipGoalDirection().equals(ShipStraightOnDirection.Forward)) {
          refreshScanAndRadar(shipId);

          if (shipAppComponent.isShipBlocked()) {
            handleCollisionWhileMoving_South(shipId);
            continue;
          }

          recoverFromCirculationMovement(shipId);

          navigate(shipId, ShipStraightOnDirection.Forward.name(), Rudder.Center.getKey());
          continue;
        }
        else {
          refreshScanAndRadar(shipId);

          if (shipAppComponent.isShipBlocked()) {
            handleCollisionWhileMoving_South(shipId);
            continue;
          }

          recoverFromCirculationMovement(shipId);

          navigate(shipId, ShipStraightOnDirection.Backward.name(), Rudder.Center.getKey());
          continue;
        }
      }


      if (shipAppComponent.isShipFacingWest()) {
        refreshScanAndRadar(shipId);

        Helper.sleepForMillis(delayMillis);
        navigate(shipId, Course.Backward.getKey(), Rudder.Left.getKey());
      }  // if ship heading to west

      startFollowWall(shipId);
    }

    return false;
  }

  private void handleCollisionWhileMoving_South(String shipId) {
    Helper.sleepForMillis(delayMillis);
    navigate(shipId, Course.Backward.getKey(), Rudder.Center.getKey());

    startFollowWall(shipId);

    while (shipAppComponent.isShipBlocked()) {
      Helper.sleepForMillis(delayMillis);
      navigate(shipId, Course.Backward.getKey(), Rudder.Center.getKey());

      startFollowWall(shipId);
    }
  }

  private boolean driveableTo_SouthSeaBoundary() {
    return shipAppComponent.getDriveableToShipGoalDirection() != ShipStraightOnDirection.Disabled && shipAppComponent.getShipSector()
                                                                                                                     .getY() != 0;
  }





  // ===== Autopilot - to west
  private boolean moveShipToWestSeaBoundary(String shipId) {
    Vec2D west = ShipGoalDirection.WEST.getKey();
    shipAppComponent.setShipGoalDirection(west);

    refreshScanAndRadar(shipId);

    while (!shipAppComponent.isShipAtWestBoundary()) {
      if (driveableTo_WestSeaBoundary()) {
        if (shipAppComponent.getDriveableToShipGoalDirection().equals(ShipStraightOnDirection.Forward)) {
          refreshScanAndRadar(shipId);

          if (shipAppComponent.isShipBlocked()) {
            handleCollisionWhileMoving_West(shipId);
            continue;
          }

          recoverFromCirculationMovement(shipId);

          navigate(shipId, ShipStraightOnDirection.Forward.name(), Rudder.Center.getKey());
          continue;
        }
        else {
          refreshScanAndRadar(shipId);

          if (shipAppComponent.isShipBlocked()) {
            handleCollisionWhileMoving_West(shipId);
            continue;
          }

          recoverFromCirculationMovement(shipId);

          navigate(shipId, ShipStraightOnDirection.Backward.name(), Rudder.Center.getKey());
          continue;
        }
      }

      startFollowWall(shipId);
    }

    return true;
  }

  private boolean driveableTo_WestSeaBoundary() {
    return shipAppComponent.getDriveableToShipGoalDirection() != ShipStraightOnDirection.Disabled && shipAppComponent.getShipSector()
                                                                                                                     .getX() != 0;
  }

  private void startFollowWall(String shipId) {
    Vec2D direction = shipAppComponent.getShipGoalDirection();
    ShipGoalDirection goalDirection = ShipGoalDirection.fromVec2D(direction);

    if (!shipAppComponent.isShipAtSouthBoundary() && goalDirection != null) {
      switch (goalDirection) {
        case WEST -> {
          wallFollowMode = WallFollowMode.LEFT;
          wallFollowLeft(shipId);
        }
        case SOUTH -> {
          wallFollowMode = WallFollowMode.RIGHT;
          wallFollowRight(shipId);
        }
      }
    }
    else {
      wallFollowMode = WallFollowMode.RIGHT;
      wallFollowRight(shipId);
    }

    refreshScanAndRadar(shipId);
  }

  private void handleCollisionWhileMoving_West(String shipId) {
    System.out.println("Ship blocked and will be handled by ship collision");
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

  private boolean isCirculationMovementDetected(String shipId) {
    String mapKey =
        shipId + shipAppComponent.getShipSector() + shipAppComponent.getShipDirection() + wallFollowMode.name();

    int count = stepStateCounter.getOrDefault(mapKey, 0) + 1;
    stepStateCounter.put(mapKey, count);

    // sonst wächst die Map unendlich
    if (stepStateCounter.size() > 100) {
      stepStateCounter.clear();
    }

    return count >= 3;
  }

  private void refreshScanAndRadar(String shipId) {
    scan(shipId);

    RadarResponse radarResponse = radar(shipId);
    shipAppComponent.setRadarResponse(radarResponse);

    shipAppComponent.driveableToShipGoalDirection();
  }

  private void wallFollowLeft(String shipId) {
    System.out.println("start wall follow left");
    refreshScanAndRadar(shipId);

    boolean foundCirculation = isCirculationMovementDetected(shipId);

    boolean isForwardCenterBlocked = forwardCenterBlocked.getOrDefault(shipId, false);

    shipAppComponent.calculateNavigableDirections();

    // === SONDERFALL ===, da Schiff ist in Kreisbewegung
    if (foundCirculation) {
      handleShipCirculation(shipId);
      return;
    }

    // === NORMALFALL ===, also bevor das Schiff sich in einer Kreisbewegung gesetzt hat.
    if (shipAppComponent.driveableToDirection(Course.Forward.getKey(), Rudder.Left.getKey())) {
      forwardCenterBlocked.put(shipId, false);

      Helper.sleepForMillis(delayMillis);
      navigate(shipId, Course.Forward.getKey(), Rudder.Left.getKey());
      shipAppComponent.driveableToShipGoalDirection();
      return;
    }

    if ((shipAppComponent.driveableToDirection(Course.Forward.getKey(), Rudder.Center.getKey())
        && !isForwardCenterBlocked)) {
      // Forward + Center wurde gerade gefahren – bis ein Forward + Left oder Forward + Right erfolgt, darf Forward +
      // Center nicht erneut gewählt werden
      forwardCenterBlocked.put(shipId, true);

      navigate(shipId, Course.Forward.getKey(), Rudder.Center.getKey());
      shipAppComponent.driveableToShipGoalDirection();
      return;
    }

    if (shipAppComponent.driveableToDirection(Course.Forward.getKey(), Rudder.Right.getKey())) {
      forwardCenterBlocked.put(shipId, false);

      Helper.sleepForMillis(delayMillis);
      navigate(shipId, Course.Forward.getKey(), Rudder.Right.getKey());
      shipAppComponent.driveableToShipGoalDirection();
      return;
    }

    Helper.sleepForMillis(delayMillis);
    navigate(shipId, Course.Backward.getKey(), Rudder.Center.getKey());
    shipAppComponent.driveableToShipGoalDirection();
  }

  private void wallFollowRight(String shipId) {
    System.out.println("start wall follow right");

    refreshScanAndRadar(shipId);

    boolean foundCirculation = isCirculationMovementDetected(shipId);

    boolean isForwardCenterBlocked = forwardCenterBlocked.getOrDefault(shipId, false);

    shipAppComponent.calculateNavigableDirections();

    // === SONDERFALL ===, da Schiff ist in Kreisbewegung
    if (foundCirculation) {
      handleShipCirculation(shipId);
      return;
    }

    // === NORMALFALL ===, also bevor das Schiff sich in einer Kreisbewegung gesetzt hat.
    if (shipAppComponent.driveableToDirection(Course.Forward.getKey(), Rudder.Right.getKey())) {
      forwardCenterBlocked.put(shipId, false);

      Helper.sleepForMillis(delayMillis);
      navigate(shipId, Course.Forward.getKey(), Rudder.Right.getKey());
      shipAppComponent.driveableToShipGoalDirection();
      return;
    }

    if ((shipAppComponent.driveableToDirection(Course.Forward.getKey(),
                                               Rudder.Center.getKey()) && !isForwardCenterBlocked)) {
      // Forward + Center wurde gerade gefahren – bis ein Forward + Left oder Forward + Right erfolgt, darf Forward +
      // Center nicht erneut gewählt werden
      forwardCenterBlocked.put(shipId, true);

      navigate(shipId, Course.Forward.getKey(), Rudder.Center.getKey());
      shipAppComponent.driveableToShipGoalDirection();
      return;
    }

    if (shipAppComponent.driveableToDirection(Course.Forward.getKey(), Rudder.Left.getKey())) {
      forwardCenterBlocked.put(shipId, false);

      Helper.sleepForMillis(delayMillis);
      navigate(shipId, Course.Forward.getKey(), Rudder.Left.getKey());
      shipAppComponent.driveableToShipGoalDirection();
      return;

    }

    Helper.sleepForMillis(delayMillis);
    navigate(shipId, Course.Backward.getKey(), Rudder.Center.getKey());
    shipAppComponent.driveableToShipGoalDirection();

  }

  private void handleShipCirculation(String shipId) {
    forwardCenterBlocked.put(shipId, true);

    Helper.sleepForMillis(delayMillis);
    navigate(shipId, Course.Backward.getKey(), Rudder.Center.getKey());

    Helper.sleepForMillis(delayMillis);
    navigate(shipId, Course.Backward.getKey(), Rudder.Center.getKey());

    shipAppComponent.driveableToShipGoalDirection();
  }

  private void recoverFromCirculationMovement(String shipId) {
    boolean foundCirculationMovement = isCirculationMovementDetected(shipId);
    if (foundCirculationMovement) {
      if (shipAppComponent.driveableToDirection(Course.Backward.getKey(), Rudder.Right.getKey())) {
        Helper.sleepForMillis(delayMillis);
        navigate(shipId, Course.Backward.getKey(), Rudder.Right.getKey());
      }
      if (shipAppComponent.driveableToDirection(Course.Backward.getKey(), Rudder.Left.getKey())) {
        Helper.sleepForMillis(delayMillis);
        navigate(shipId, Course.Backward.getKey(), Rudder.Left.getKey());
      }
    }

    refreshScanAndRadar(shipId);
  }


}