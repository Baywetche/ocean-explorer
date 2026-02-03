package com.hhs.shipapp.controller;

import com.hhs.lib.model.*;
import com.hhs.shipapp.models.ShipEntityState;
import com.hhs.shipapp.models.ShipMessage;
import com.hhs.shipapp.models.enums.Commands;
import com.hhs.shipapp.models.messages.RadarResponse;
import com.hhs.shipapp.service.ShipAppImpl;
import com.hhs.shipapp.service.ShipTransportMessage;
import com.hhs.shipapp.util.Helper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ship")
public class ShipAppController {

  private final ShipAppImpl shipAppImpl;
  private final Map<String, ShipEntityState> shipEntityStateMap;
  private final ShipTransportMessage shipTransportMessage;

  public ShipAppController(ShipAppImpl shipAppImpl, ShipTransportMessage shipTransportMessage,
      Map<String, ShipEntityState> shipEntityStateMap) {
    this.shipAppImpl = shipAppImpl;
    this.shipTransportMessage = shipTransportMessage;
    this.shipEntityStateMap = shipEntityStateMap;
  }

  @PostMapping("/launch")
  public ResponseEntity<String> launch(@RequestParam String name, @RequestParam int x, @RequestParam int y, @RequestParam int dx,
      @RequestParam int dy) {
    // Prüft, ob eine aktive Verbindung zum Server besteht. wenn keine Verbindung vorhanden ist, wird eine neue Verbindung aufgebaut.
    boolean isConnectedToShipServer = shipAppImpl.getConnectionState();
    if (!isConnectedToShipServer) {
      shipAppImpl.connectShipClientToShipServer();
    }

    Vec2D direction = new Vec2D(dx, dy);
    Vec2D sector = new Vec2D(x, y);

    boolean isSectorFree = shipTransportMessage.isSectorFree(sector);
    if (!isSectorFree) {
      System.out.println("The ship cannot be created because the sector is not free!");
      return ResponseEntity.ok("Error");
    }

    // ein Schiff wird hier erstellt
    List<ShipMessage> shipMessages = shipAppImpl.launch(name, sector, direction);
    String shipId = shipMessages.getFirst().getId();

    ShipEntityState state = Helper.updateShipEntityStateMap(shipId, name, sector, direction);
    shipEntityStateMap.put(shipId, state);

    if (shipMessages.getFirst().getCmd() != Commands.launched) {
      return ResponseEntity.ok("Error");
    }

    /* save sector data into DB*/
    SectorData sectorData = new SectorData(shipId, x, y);
    shipTransportMessage.saveSectorData(sectorData);

    /* save ship data into DB*/
    shipTransportMessage.saveShipData(new ShipData(shipId, name, x, y, dx, dy));

    return ResponseEntity.ok(shipId);
  }

  @PostMapping("/radar")
  public ResponseEntity<RadarResponse> radar(@RequestParam String shipId) {
    // Prüft, ob eine aktive Verbindung zum Server besteht
    boolean isConnectedToShipServer = shipAppImpl.getConnectionState();
    if (!isConnectedToShipServer) {
      return ResponseEntity.ok(new RadarResponse());
    }

    //check, if shipId exist in DB
    boolean shipIdExists = shipTransportMessage.isShipIdExists(shipId);
    if (!shipIdExists) {
      return ResponseEntity.ok(new RadarResponse());
    }

    // send radar-cmd to server
    List<ShipMessage> shipMessages = shipAppImpl.radar();

    ShipEntityState state = shipEntityStateMap.get(shipId);

    RadarResponse radarResponse = Helper.getRadarResponse(shipMessages.getFirst(), state.getSector(), state.getDirection());

    /* save sector data into DB*/
    Helper.persistSectorData(shipId, shipTransportMessage, shipMessages);

    return ResponseEntity.ok(radarResponse);
  }

  @PostMapping("/scan")
  public ResponseEntity<Integer> scan(@RequestParam String shipId) {
    // Prüft, ob eine aktive Verbindung zum Server besteht
    boolean isConnectedToShipServer = shipAppImpl.getConnectionState();
    if (!isConnectedToShipServer) {
      return ResponseEntity.ok(-2_000_000_000);
    }

    //check, if shipId exist in DB
    boolean shipIdExists = shipTransportMessage.isShipIdExists(shipId);
    if (!shipIdExists) {
      return ResponseEntity.ok(-2_000_000_000);
    }

    // send scan-cmd to server
    List<ShipMessage> shipMessages = shipAppImpl.scan();

    // update sector data in DB
    Helper.updateSectorData(shipId, shipTransportMessage, shipMessages);

    return ResponseEntity.ok(shipMessages.getFirst().getDepth());
  }

  @PostMapping("/navigate")
  public ResponseEntity<Boolean> navigate(@RequestParam String shipId, @RequestParam String course, @RequestParam String rudder) {
    // Prüft, ob eine aktive Verbindung zum Server besteht
    boolean isConnectedToShipServer = shipAppImpl.getConnectionState();
    if (!isConnectedToShipServer) {
      return ResponseEntity.ok(false);
    }

    //check, if ship exists in Map
    ShipEntityState state = shipEntityStateMap.get(shipId);
    if (state == null) {
      System.out.println("Navigate: state is null");
      ResponseEntity.ok(false);
    }

    //check, if shipId exist in DB
    boolean shipIdExists = shipTransportMessage.isShipIdExists(shipId);
    if (!shipIdExists) {
      return ResponseEntity.ok(false);
    }

    // navigate and update data in DB if navigation succeed
    List<ShipMessage> shipMessages = shipAppImpl.navigate(course, rudder);
    boolean navigateSucceed = shipMessages.getFirst().getCmd() != Commands.crash;
    if (state != null && navigateSucceed) {
      System.out.println("navigate succeed: " + navigateSucceed);

      // update shipEntityStateMap
      Helper.updateShipEntityState(shipEntityStateMap, shipId, shipMessages, course, rudder);

      // update shipData
      Vec2D sector = new Vec2D(shipMessages.getFirst().getSector().getVec2()[0], shipMessages.getFirst().getSector().getVec2()[1]);
      Vec2D direction = new Vec2D(shipMessages.getFirst().getDir().getVec2()[0], shipMessages.getFirst().getDir().getVec2()[1]);

      ShipData shipData = new ShipData();
      shipData.setShipId(shipId);
      shipData.setShipName(Helper.extractShipNameFromShipId(shipId));
      shipData.setSectorX(sector.getX());
      shipData.setSectorY(sector.getY());
      shipData.setDirectionX(direction.getX());
      shipData.setDirectionY(direction.getY());

      shipTransportMessage.updateShipData(shipData);

      return ResponseEntity.ok(true);
    } else {
      // connection must be closed after crash
      exit(shipId);

      return ResponseEntity.ok(false);
    }
  }

  @PostMapping("/autoPilot")
  public ResponseEntity<AutoPilotData> autoPilot(@RequestParam String shipId) {
    ResponseEntity<RadarResponse> radarResponse = radar(shipId);
    List<Sector> notNavigable = radarResponse.getBody().getNotNavigable();
    RelativeCoordinateSystem relativeCoordinateSystem = new RelativeCoordinateSystem(shipEntityStateMap.get(shipId).getDirection());

    Sector directionToEast = new Sector(relativeCoordinateSystem.getCoordinates().get(2));
    Sector directionToWest = new Sector(relativeCoordinateSystem.getCoordinates().get(6));

    if (!notNavigable.contains(directionToEast)) {
      notNavigable.add(directionToEast); // Hinzufügung von Ost-Richtung zu der nicht navigierbaren Liste
    }

    if (!notNavigable.contains(directionToWest)) {
      notNavigable.add(directionToWest); // Hinzufügung von West-Richtung zu der nicht navigierbaren Liste
    }

    System.out.println(notNavigable);

    ResponseEntity<Integer> scanResponse = scan(shipId);








 /*
    ResponseEntity<Boolean> navigateResponse = navigate(shipId, "Forward", "Center");

    ShipEntityState state = shipEntityStateMap.get(shipId);*/

    return ResponseEntity.ok(new AutoPilotData());
  }

  @PostMapping("/exit")
  public ResponseEntity<Boolean> exit(@RequestParam String shipId) {
    // Prüft, ob eine aktive Verbindung zum Server besteht
    boolean isConnectedToShipServer = shipAppImpl.getConnectionState();
    if (!isConnectedToShipServer) {
      return ResponseEntity.ok(false);
    }

    //check, if shipId exist in DB
    boolean shipIdExists = shipTransportMessage.isShipIdExists(shipId);
    if (!shipIdExists) {
      return ResponseEntity.ok(false);
    }

    // delete shipData from shipData database
    shipTransportMessage.removeShipData(shipId);

    // send {"cmd": "exit"} to server and close ship Client Connection
    return ResponseEntity.ok(shipAppImpl.exit());
  }
}








