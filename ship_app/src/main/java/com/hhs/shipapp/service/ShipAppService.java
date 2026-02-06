package com.hhs.shipapp.service;

import com.hhs.lib.model.*;
import com.hhs.shipapp.models.ShipEntityState;
import com.hhs.shipapp.models.ShipMessage;
import com.hhs.shipapp.models.enums.Commands;
import com.hhs.shipapp.models.messages.RadarResponse;
import com.hhs.shipapp.util.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ShipAppService {

  private final ShipAppImpl shipAppImpl;
  private final ShipTransportMessage shipTransportMessage;
  private final Map<String, ShipEntityState> shipEntityStateMap;
  private static final Logger log = LoggerFactory.getLogger(ShipAppService.class);

  public ShipAppService(ShipAppImpl shipAppImpl, ShipTransportMessage shipTransportMessage,
      Map<String, ShipEntityState> shipEntityStateMap) {
    this.shipAppImpl = shipAppImpl;
    this.shipTransportMessage = shipTransportMessage;
    this.shipEntityStateMap = shipEntityStateMap;
  }

  // Hilfsmethode ab hier
  private void ensureConnectedToShipServer() {
    if (!shipAppImpl.getConnectionState()) {
      shipAppImpl.connectShipClientToShipServer();
      // Optional: hier prüfen, ob connect erfolgreich war
      if (!shipAppImpl.getConnectionState()) {
        log.info("connection to ship server failed");
      }
    }
  }

  private void saveSectorAndShipData(String shipId, String name, int x, int y, int dx, int dy) {
    SectorData sectorData = new SectorData(shipId, x, y);
    sectorData.setGround(Ground.Harbour);
    shipTransportMessage.saveSectorData(sectorData);

    ShipData shipData = new ShipData(shipId, name, x, y, dx, dy);
    shipTransportMessage.saveShipData(shipData);
  }
  // Hilfsmethode Ende

  /**
   * Startet ein neues Schiff im angegebenen Sektor mit Richtung.
   *
   * @return shipId bei Erfolg oder wirft eine Exception bei Fehlern
   */
  public String launchShip(String name, int x, int y, int dx, int dy) {
    if (x < 0 || y < 0) {
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

    // Schiff starten
    List<ShipMessage> shipMessages = shipAppImpl.launch(name, sector, direction);
    ShipMessage firstMessage = shipMessages.getFirst();

    String shipId = firstMessage.getId();

    if (firstMessage.getCmd() != Commands.launched) {
      return "Ship launch failed: " + firstMessage.getCmd();
    }

    // State aktualisieren
    ShipEntityState state = Helper.updateShipEntityStateMap(shipId, name, sector, direction);
    shipEntityStateMap.put(shipId, state);

    // Daten persistieren
    saveSectorAndShipData(shipId, name, x, y, dx, dy);

    return shipId;
  }

  /**
   * Führt einen Radar-Scan für das angegebene Schiff durch.
   *
   * @param shipId Die ID des Schiffs
   * @return RadarResponse mit den gescannten Informationen
   * @throws IllegalArgumentException wenn das Schiff nicht existiert
   * @throws IllegalStateException bei Verbindungs- oder Ausführungsproblemen
   */
  public RadarResponse radar(String shipId) {
    ensureConnectedToShipServer();

    // Prüfen, ob das Schiff in der Datenbank existiert
    if (!shipTransportMessage.isShipIdExists(shipId)) {
      throw new IllegalArgumentException("Ship with ID " + shipId + " does not exist");
    }

    // Radar-Befehl an den Server senden
    List<ShipMessage> shipMessages = shipAppImpl.radar();
    if (shipMessages == null || shipMessages.isEmpty()) {
      throw new IllegalStateException("No response received from radar command");
    }

    ShipMessage radarMessage = shipMessages.getFirst();

    // Aktuellen Zustand des Schiffs holen
    ShipEntityState state = shipEntityStateMap.get(shipId);
    if (state == null) {
      throw new IllegalStateException("No state found for ship " + shipId);
    }

    // Radar-Response erstellen
    RadarResponse radarResponse = Helper.getRadarResponse(radarMessage, state.getSector(), state.getDirection());

    // Gefundene Sektoren persistent speichern
    Helper.persistSectorData(shipId, shipTransportMessage, shipMessages);

    return radarResponse;
  }

  /**
   * Führt einen Scan-Befehl für das angegebene Schiff aus und gibt die gemessene Tiefe zurück.
   *
   * @param shipId Die ID des Schiffs
   * @return die gemessene Tiefe (als Integer)
   * @throws IllegalArgumentException wenn das Schiff nicht existiert
   * @throws IllegalStateException bei Verbindungsproblemen oder ungültiger Antwort
   */
  public int scan(String shipId) {
    ensureConnectedToShipServer();

    // Prüfen, ob das Schiff in der Datenbank existiert
    if (!shipTransportMessage.isShipIdExists(shipId)) {
      throw new IllegalArgumentException("Ship with ID " + shipId + " does not exist");
    }

    // Scan-Befehl an den Server senden
    List<ShipMessage> shipMessages = shipAppImpl.scan();
    if (shipMessages == null || shipMessages.isEmpty()) {
      throw new IllegalStateException("No response received from scan command");
    }

    ShipMessage scanMessage = shipMessages.getFirst();

    // Sector-Daten aktualisieren
    Helper.updateSectorData(shipId, shipTransportMessage, shipMessages);

    // Tiefe zurückgeben
    return scanMessage.getDepth();
  }

  /**
   * Führt eine Navigation für das angegebene Schiff aus.
   *
   * @param shipId Die ID des Schiffs
   * @param course Der gewünschte Kurs (z. B. "left", "right", "straight", ...)
   * @param rudder Der gewünschte Rudereinschlag
   * @return true wenn die Navigation erfolgreich war, false bei Crash oder Fehler
   * @throws IllegalArgumentException bei ungültigem shipId oder fehlendem State
   * @throws IllegalStateException bei Verbindungsproblemen
   */
  public boolean navigate(String shipId, String course, String rudder) {
    ensureConnectedToShipServer();

    // Prüfen, ob das Schiff im In-Memory-State existiert
    ShipEntityState state = shipEntityStateMap.get(shipId);
    if (state == null) {
      throw new IllegalArgumentException("No state found for ship: " + shipId);
    }

    // Prüfen, ob das Schiff in der Datenbank existiert
    if (!shipTransportMessage.isShipIdExists(shipId)) {
      throw new IllegalArgumentException("Ship with ID " + shipId + " does not exist in database");
    }

    // Navigation ausführen
    List<ShipMessage> shipMessages = shipAppImpl.navigate(course, rudder);
    if (shipMessages == null || shipMessages.isEmpty()) {
      throw new IllegalStateException("No response received from navigate command");
    }

    ShipMessage firstMessage = shipMessages.getFirst();
    boolean success = firstMessage.getCmd() != Commands.crash;

    if (success) {
      // State aktualisieren
      Helper.updateShipEntityState(shipEntityStateMap, shipId, shipMessages, course, rudder);

      // Neue Position und Richtung extrahieren
      Vec2D newSector = new Vec2D(firstMessage.getSector().getVec2()[0], firstMessage.getSector().getVec2()[1]);
      Vec2D newDirection = new Vec2D(firstMessage.getDir().getVec2()[0], firstMessage.getDir().getVec2()[1]);

      // ShipData für Update vorbereiten
      ShipData updatedShipData = new ShipData();
      updatedShipData.setShipId(shipId);
      updatedShipData.setShipName(Helper.extractShipNameFromShipId(shipId));
      updatedShipData.setSectorX(newSector.getX());
      updatedShipData.setSectorY(newSector.getY());
      updatedShipData.setDirectionX(newDirection.getX());
      updatedShipData.setDirectionY(newDirection.getY());

      // In DB aktualisieren
      shipTransportMessage.updateShipData(updatedShipData);

      return true;
    } else {
      // Bei Crash: Verbindung beenden
      exit(shipId);

      return false;
    }
  }

  /**
   * Beendet die Verbindung eines Schiffs zum Server und entfernt die zugehörigen Daten.
   *
   * @param shipId Die ID des Schiffs, das beendet werden soll
   * @return true, wenn der Exit-Befehl erfolgreich gesendet und die Verbindung geschlossen wurde
   * @throws IllegalArgumentException wenn das Schiff nicht existiert
   * @throws IllegalStateException bei Verbindungsproblemen
   */
  public boolean exit(String shipId) {
    ensureConnectedToShipServer();

    // Prüfen, ob das Schiff in der Datenbank existiert
    if (!shipTransportMessage.isShipIdExists(shipId)) {
      throw new IllegalArgumentException("Ship with ID " + shipId + " does not exist");
    }

    // Schiff-Daten aus der Datenbank entfernen
    shipTransportMessage.removeShipData(shipId);

    // Exit-Befehl an den Server senden und Verbindung schließen
    boolean success = shipAppImpl.exit();

    // Optional: In-Memory-State bereinigen (empfohlen)
    shipEntityStateMap.remove(shipId);

    return success;
  }

  /**
   * Führt einen Autopilot-Schritt für das angegebene Schiff aus.
   * Scannt, analysiert Umgebung, plant nächste Bewegung.
   *
   * @param shipId Die ID des Schiffs
   * @return AutoPilotData mit Ergebnissen des Schritts (aktuell noch minimal)
   * @throws IllegalArgumentException bei ungültigem Schiff oder fehlendem Zustand
   * @throws IllegalStateException bei Verbindungs- oder Ausführungsproblemen
   */
  public AutoPilotData runAutoPilot(String shipId) {
    ensureConnectedToShipServer();

    // 1. Schiff-Status prüfen
    ShipEntityState state = shipEntityStateMap.get(shipId);
    if (state == null) {
      throw new IllegalArgumentException("No state found for ship: " + shipId);
    }

    if (!shipTransportMessage.isShipIdExists(shipId)) {
      throw new IllegalArgumentException("Ship with ID " + shipId + " does not exist in database");
    }

    // 2. Radar ausführen
    RadarResponse radarResponse = radar(shipId);
    List<Sector> notNavigable = new ArrayList<>(radarResponse.getNotNavigable());

    // 3. Relative Koordinaten berechnen
    RelativeCoordinateSystem relCoord = new RelativeCoordinateSystem(state.getDirection());

    // Ost und West als grundsätzlich nicht navigierbar behandeln
    Sector east = new Sector(relCoord.getCoordinates().get(2));
    Sector west = new Sector(relCoord.getCoordinates().get(6));

    if (!notNavigable.contains(east)) {
      notNavigable.add(east);
    }
    if (!notNavigable.contains(west)) {
      notNavigable.add(west);
    }

    System.out.println("Not navigable sectors: " + notNavigable);

    // 4. Scan ausführen
    int depth = scan(shipId);
    // Optional: Tiefe könnte später in die Entscheidung einfließen

    // 5. Blockierende Schiffe / Hindernisse ermitteln
    Optional<Vec2D> blockingShipSector = Helper.getShipBlockingSector(state, radarResponse);
    Vec2D newGoalSector = Helper.calcNewGoalShipSectorAfterBlocking(state);

    // 6. Erlaubte Nachbarfelder berechnen
    List<Vec2D> allowedSurroundingFields = Helper.calcAllowedSurroundingFields(state, notNavigable);

    // 7. Route-Planung / Baum aufbauen
    // Hier entscheidest du, ob du den Baum jedes Mal neu baust oder speicherst
    Helper.getRouteTree(state, notNavigable);

    // TODO: Hier kommt die eigentliche Entscheidungslogik
    // Beispiel: einfachstes Verhalten → erstes erlaubtes Feld nehmen
    Vec2D nextTarget = null;
    if (!allowedSurroundingFields.isEmpty()) {
      nextTarget = allowedSurroundingFields.get(0); // ← sehr einfach – später verbessern!
      // oder: kürzeste Route, A*, Priorität nach Richtung zum Ziel, ...
    }

    // TODO: Navigation ausführen (wenn gewünscht)
    // boolean navigateSuccess = navigateTo(shipId, nextTarget); // ← eigene Methode

    // 8. Ergebnis zusammenfassen
    AutoPilotData result = new AutoPilotData();
    // result.setNextMove(nextTarget);
    // result.setSuccess(navigateSuccess);
    // result.setDepth(depth);
    // result.setBlockedBy(blockingShipSector.orElse(null));

    // Optional: Route-Plan speichern
    // shipTransportMessage.saveRoutePlan(...);

    return result;
  }

  // Hilfsmethode – falls du später navigieren willst
  // private boolean navigateTo(String shipId, Vec2D target) { ... }
}