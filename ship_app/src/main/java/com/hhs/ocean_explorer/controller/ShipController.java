package com.hhs.ocean_explorer.controller;

import com.hhs.ocean_explorer.models.ShipMessage;
import com.hhs.ocean_explorer.models.Vec2D;
import com.hhs.ocean_explorer.service.ShipAppImpl;
import com.hhs.ocean_explorer.service.ShipTransportMessage;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/ship")
public class ShipController {

  private final ShipAppImpl shipAppImpl;
  private final ShipTransportMessage shipTransportMessage;

  @PostMapping("/launch")
  public ResponseEntity<List<ShipMessage>> launch(
      @RequestParam String name,
      @RequestParam int x, @RequestParam int y,
      @RequestParam int dx, @RequestParam int dy) {

    Vec2D sector = new Vec2D(x, y);
    Vec2D dir = new Vec2D(dx, dy);

    List<ShipMessage> strings = shipAppImpl.launch(name, sector, dir);
    return ResponseEntity.ok(strings);
  }

  @PostMapping("/navigate")
  public ResponseEntity<List<ShipMessage>> navigate(
      @RequestParam String rudder,
      @RequestParam String course) {

    List<ShipMessage> message = shipAppImpl.navigate(rudder, course);

    return ResponseEntity.ok(message);
  }

  @PostMapping("/exit")
  public ResponseEntity<String> exit() {
    shipAppImpl.exit();
    return ResponseEntity.ok("sent exit");
  }

  @PostMapping("/scan")
  public ResponseEntity<List<ShipMessage>> scan() {
    List<ShipMessage> messages = shipAppImpl.scan();
    return ResponseEntity.ok(messages);
  }

  @PostMapping("/radar")
  public ResponseEntity<List<ShipMessage>> radar() {
    List<ShipMessage> messages = shipAppImpl.radar();
    shipTransportMessage.sendMessage();
    return ResponseEntity.ok(messages);
  }

}