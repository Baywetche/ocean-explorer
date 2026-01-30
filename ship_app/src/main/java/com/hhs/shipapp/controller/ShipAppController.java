package com.hhs.shipapp.controller;

import com.hhs.lib.model.SectorData;
import com.hhs.lib.model.ShipData;
import com.hhs.lib.model.Vec2D;
import com.hhs.shipapp.models.ShipEntityState;
import com.hhs.shipapp.models.ShipMessage;
import com.hhs.shipapp.models.enums.Commands;
import com.hhs.shipapp.models.messages.RadarResponse;
import com.hhs.shipapp.service.ShipApp;
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

  private final ShipApp shipApp;
  private final Map<String, ShipEntityState> shipEntityStateMap;
  private final ShipTransportMessage shipTransportMessage;

  public ShipAppController(ShipApp shipApp, ShipTransportMessage shipTransportMessage, Map<String, ShipEntityState> shipEntityStateMap) {
    this.shipApp = shipApp;
    this.shipTransportMessage = shipTransportMessage;
    this.shipEntityStateMap = shipEntityStateMap;
  }

  @PostMapping("/launch")
  public ResponseEntity<String> launch(@RequestParam String name, @RequestParam int x, @RequestParam int y, @RequestParam int dx,
      @RequestParam int dy) {
    // Prüft, ob eine aktive Verbindung zum Server besteht. wenn keine Verbindung vorhanden ist, wird eine neue Verbindung aufgebaut.
    boolean isConnectedToShipServer = shipApp.getConnectionState();
    if (!isConnectedToShipServer) {
      shipApp.connectShipClientToShipServer();
    }

    Vec2D direction = new Vec2D(dx, dy);
    Vec2D sector = new Vec2D(x, y);

    boolean isSectorFree = shipTransportMessage.isSectorFree(sector);
    if (!isSectorFree) {
      System.out.println("The ship cannot be created because the sector is not free!");
      return ResponseEntity.ok("Error");
    }

    // ein Schiff wird hier erstellt
    List<ShipMessage> shipMessages = shipApp.launch(name, sector, direction);
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
    boolean isConnectedToShipServer = shipApp.getConnectionState();
    if (!isConnectedToShipServer) {
      return ResponseEntity.ok(new RadarResponse());
    }

    //check, if shipId exist in DB
    boolean shipIdExists = shipTransportMessage.isShipIdExists(shipId);
    if (!shipIdExists) {
      return ResponseEntity.ok(new RadarResponse());
    }

    // send radar-cmd to server
    List<ShipMessage> shipMessages = shipApp.radar();

    ShipEntityState state = shipEntityStateMap.get(shipId);

    RadarResponse radarResponse = Helper.getRadarResponse(shipMessages.getFirst(), state.getSector(), state.getDirection());

    /* save sector data into DB*/
    Helper.persistSectorData(shipId, shipTransportMessage, shipMessages);

    return ResponseEntity.ok(radarResponse);
  }

  @PostMapping("/navigate")
  public ResponseEntity<Boolean> navigate(@RequestParam String shipId, @RequestParam String course, @RequestParam String rudder) {
    // Prüft, ob eine aktive Verbindung zum Server besteht
    boolean isConnectedToShipServer = shipApp.getConnectionState();
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
    List<ShipMessage> shipMessages = shipApp.navigate(course, rudder);
    boolean navigateSucceed = shipMessages.getFirst().getCmd() != Commands.crash;
    if (state != null && navigateSucceed) {
      System.out.println("navigate succeed: " + navigateSucceed);
      Helper.updateShipEntityState(shipEntityStateMap, shipId, shipMessages, course, rudder);

      ShipData shipData = new ShipData();
      shipData.setShipId(shipId);
      shipData.setShipName(Helper.extractShipNameFromShipId(shipId));
      shipData.setSectorX(shipMessages.getFirst().getSector().getVec2()[0]);
      shipData.setSectorY(shipMessages.getFirst().getSector().getVec2()[1]);
      shipData.setDirectionX(shipMessages.getFirst().getDir().getVec2()[0]);
      shipData.setDirectionY(shipMessages.getFirst().getDir().getVec2()[1]);

      shipTransportMessage.updateShipData(shipData);

      return ResponseEntity.ok(true);
    } else {
      // connection must be closed after crash
      exit(shipId);

      return ResponseEntity.ok(false);
    }
  }

  @PostMapping("/scan")
  public ResponseEntity<Integer> scan(@RequestParam String shipId) {
    // Prüft, ob eine aktive Verbindung zum Server besteht
    boolean isConnectedToShipServer = shipApp.getConnectionState();
    if (!isConnectedToShipServer) {
      return ResponseEntity.ok(-2_000_000_000);
    }

    //check, if shipId exist in DB
    boolean shipIdExists = shipTransportMessage.isShipIdExists(shipId);
    if (!shipIdExists) {
      return ResponseEntity.ok(-2_000_000_000);
    }

    // send scan-cmd to server
    List<ShipMessage> shipMessages = shipApp.scan();

    // update sector data in DB
    Helper.updateSectorData(shipId, shipTransportMessage, shipMessages);

    return ResponseEntity.ok(shipMessages.getFirst().getDepth());
  }

  @PostMapping("/exit")
  public ResponseEntity<Boolean> exit(@RequestParam String shipId) {
    // Prüft, ob eine aktive Verbindung zum Server besteht
    boolean isConnectedToShipServer = shipApp.getConnectionState();
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
    return ResponseEntity.ok(shipApp.exit());
  }

}