package com.hhs.shipapp.controller;

import com.hhs.lib.model.SectorData;
import com.hhs.lib.model.ShipData;
import com.hhs.lib.model.Vec2D;
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
  public ResponseEntity<String> launch(@RequestParam String name, @RequestParam int x, @RequestParam int y,
                                       @RequestParam int dx, @RequestParam int dy) {

    Vec2D direction = new Vec2D(dx, dy);
    Vec2D sector = new Vec2D(x, y);

    boolean isSectorFree = shipTransportMessage.isSectorFree(sector);
    if (!isSectorFree) {
      System.out.println("isSectorFree: " + false);
      return ResponseEntity.ok("Error");
    }

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
    List<ShipMessage> shipMessages = shipAppImpl.radar();

    ShipEntityState state = shipEntityStateMap.get(shipId);

    RadarResponse radarResponse = Helper.getRadarResponse(shipMessages.getFirst(), state.getSector(),
                                                          state.getDirection());

    /* save sector data into DB*/
    Helper.persistSectorData(shipId, shipTransportMessage, shipMessages);

    return ResponseEntity.ok(radarResponse);
  }

  @PostMapping("/navigate")
  public ResponseEntity<Boolean> navigate(@RequestParam String shipId, @RequestParam String course,
                                          @RequestParam String rudder) {
    ShipEntityState state = shipEntityStateMap.get(shipId);
    System.out.println(state);
    if (state == null) {
      System.out.println("Navigate: state is null");
      ResponseEntity.ok(false);
    }

    //TODO check, is any ship at the sector, if sector empty, move it to this sector, otherwise return because crash
    // -> false (ferne Zukunft)

    List<ShipMessage> shipMessages = shipAppImpl.navigate(course, rudder);

    boolean navigateSucceed = shipMessages.getFirst().getCmd() != Commands.crash;
    if (state != null && navigateSucceed) {
      System.out.println("navigate succeed?: " + navigateSucceed);
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
    }
    else {
//      exit(shipId);

      return ResponseEntity.ok(false);
    }
  }

  @PostMapping("/scan")
  public ResponseEntity<List<ShipMessage>> scan(@RequestParam String shipId) {
    List<ShipMessage> shipMessages = shipAppImpl.scan();

    Helper.updateSectorData(shipId, shipTransportMessage, shipMessages);

    return ResponseEntity.ok(shipMessages);
  }

  @PostMapping("/exit")
  public ResponseEntity<String> exit(@RequestParam String shipId) {
    //
    // TODO delete shipData from shipData database
    shipTransportMessage.removeShipData(shipId);



    // TODO disconnect connection

    shipAppImpl.exit();
    return ResponseEntity.ok("sent exit");
  }

}















 /* @PostMapping("/launch")
  public ResponseEntity<String> launch(@RequestBody ShipData shipData) {
    //TODO prüfen, ob das gegebene Sector bereits mit einem Schiff belegt ist. wenn nicht belegt, dann ein Schiff
    // erstellen und anschließend den User entsprechend informieren

    Vec2D sector = new Vec2D(shipData.getSectorX(), shipData.getSectorY());
    Vec2D direction = new Vec2D(shipData.getDirectionX(), shipData.getDirectionY());

    boolean isSectorFree = shipTransportMessage.isSectorFree(sector);
    if (!isSectorFree) {
      return ResponseEntity.ok("Error");
    }

    List<ShipMessage> shipMessages = shipAppImpl.launch(shipData.getShipName(), sector, direction);
    String shipId = shipMessages.getFirst().getId();

    ShipEntityState state = Helper.updateShipEntityStateMap(shipId, shipData.getShipName(), sector, direction);
    shipEntityStateMap.put(shipId, state);

    if (shipMessages.getFirst().getCmd() != Commands.launched) {
      return ResponseEntity.ok("Error");
    }

  *//*  ShipData shipDataToSaveInDB = new ShipData();
    shipDataToSaveInDB.setShipId(shipId);
    shipDataToSaveInDB.setShipName(shipData.getShipName());
    shipDataToSaveInDB.setSectorX(shipData.getSectorX());
    shipDataToSaveInDB.setSectorY(shipData.getSectorY());
    shipDataToSaveInDB.setDirectionX(shipData.getDirectionX());
    shipDataToSaveInDB.setDirectionY(shipData.getDirectionY());

    shipTransportMessage.saveShipData(shipDataToSaveInDB);*//*

    return ResponseEntity.ok(shipId);
}
*/







