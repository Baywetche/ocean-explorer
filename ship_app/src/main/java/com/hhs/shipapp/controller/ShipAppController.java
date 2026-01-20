package com.hhs.shipapp.controller;

import com.hhs.lib.model.Vec2D;
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

@RestController
@RequestMapping("/api/ship")
public class ShipAppController {

  private final ShipAppImpl shipAppImpl;
  private final ShipTransportMessage shipTransportMessage;

  private Vec2D sectorAtShipPosition;
  private Vec2D shipDirection;

  public ShipAppController(ShipAppImpl shipAppImpl, ShipTransportMessage shipTransportMessage) {
    this.shipAppImpl = shipAppImpl;
    this.shipTransportMessage = shipTransportMessage;
  }

  @PostMapping("/launch")
  public ResponseEntity<String> launch(@RequestParam String name, @RequestParam int x, @RequestParam int y, @RequestParam int dx,
      @RequestParam int dy) {

    //TODO prüfen, ob das gegebene Sector bereits mit einem Schiff belegt ist. wenn nicht belegt dann ein Schiff
    // erstellen und anschließend den User informieren

    sectorAtShipPosition = new Vec2D(x, y);
    shipDirection = new Vec2D(dx, dy);

    List<ShipMessage> shipMessages = shipAppImpl.launch(name, sectorAtShipPosition, shipDirection);

    if (shipMessages.getFirst().getCmd() != Commands.launched) {
      return ResponseEntity.ok("Error");
    }

    return ResponseEntity.ok(shipMessages.getFirst().getId());
  }

  @PostMapping("/navigate")
  public ResponseEntity<Boolean> navigate(@RequestParam String shipId, @RequestParam String course, @RequestParam String rudder) {

    List<ShipMessage> shipMessages = shipAppImpl.navigate(course, rudder);

    shipDirection = Helper.updateShipDirection(shipMessages);
    sectorAtShipPosition = Helper.updateSectorAtShipPosition(shipMessages);

    return ResponseEntity.ok(shipMessages.getFirst().getCmd() != Commands.crash);
  }

  @PostMapping("/radar")
  public ResponseEntity<RadarResponse> radar(@RequestParam String shipId) {
    List<ShipMessage> shipMessages = shipAppImpl.radar();

    RadarResponse radarResponse = Helper.getRadarResponse(shipMessages.getFirst(), sectorAtShipPosition, shipDirection);

    return ResponseEntity.ok(radarResponse);
  }

  @PostMapping("/exit")
  public ResponseEntity<String> exit(@RequestParam String shipId) {
    shipAppImpl.exit();
    return ResponseEntity.ok("sent exit");
  }

  @PostMapping("/scan")
  public ResponseEntity<List<ShipMessage>> scan(@RequestParam String shipId) {
    List<ShipMessage> shipMessages = shipAppImpl.scan();
    return ResponseEntity.ok(shipMessages);
  }

}

















