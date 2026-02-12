package com.hhs.shipapp.controller;

import com.hhs.lib.model.AutoPilotData;
import com.hhs.lib.model.Sector;
import com.hhs.lib.model.Vec2D;
import com.hhs.shipapp.models.RadarResponse;
import com.hhs.shipapp.service.ShipAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ship")
public class ShipAppController {

  private final ShipAppService shipAppService;
  private static final Logger log = LoggerFactory.getLogger(ShipAppController.class);

  public ShipAppController(ShipAppService shipAppService) {
    this.shipAppService = shipAppService;
  }

  @PostMapping("/launch")
  public ResponseEntity<String> launch(@RequestParam String name, @RequestParam int x, @RequestParam int y, @RequestParam int dx,
      @RequestParam int dy) {

    try {
      String shipId = shipAppService.launchShip(name, x, y, dx, dy);
      return ResponseEntity.ok(shipId);
    } catch (IllegalStateException e) {
      log.error("Launch failed: " + e.getMessage());
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/radar")
  public ResponseEntity<RadarResponse> radar(@RequestParam String shipId) {
    try {
      RadarResponse response = shipAppService.radar(shipId);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    } catch (IllegalStateException e) {
      log.error("Radar failed for ship {}: {}", shipId, e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Unexpected radar-error: for ship {}", shipId, e);
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/scan")
  public ResponseEntity<Integer> scan(@RequestParam String shipId) {
    try {
      return ResponseEntity.ok(shipAppService.scan(shipId));
    } catch (IllegalArgumentException | IllegalStateException e) {
      log.info("Scan failed for ship {}: {}", shipId, e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Unexpected scan error for ship {}", shipId, e);
      return ResponseEntity.internalServerError().build();
    }
  }

  @GetMapping("/navigate")
  public ResponseEntity<Vec2D> navigate(@RequestParam String shipId, @RequestParam String course, @RequestParam String rudder) {
    try {
      Vec2D direction = shipAppService.navigate(shipId, course, rudder);
      return ResponseEntity.ok(direction);
    } catch (IllegalArgumentException e) {
      log.info("Navigation failed for ship {}: {}", shipId, e.getMessage());
      return ResponseEntity.ok(null);
    } catch (IllegalStateException e) {
      log.error("Navigation error for ship {}: {}", shipId, e.getMessage());
      return ResponseEntity.ok(null);
    } catch (Exception e) {
      log.error("Unexpected navigation error for ship {}", shipId, e);
      return ResponseEntity.ok(null);
    }
  }

  @PostMapping("/exit")
  public ResponseEntity<Boolean> exit(@RequestParam String shipId) {
    try {
      return ResponseEntity.ok(shipAppService.exit(shipId));
    } catch (Exception e) {
      log.warn("Exit failed for ship {}: {}", shipId, e.getMessage());
      return ResponseEntity.ok(false);
    }
  }

  @PostMapping("/autoPilot")
  public ResponseEntity<AutoPilotData> autoPilot(@RequestParam String shipId) {
    try {
      AutoPilotData result = shipAppService.runAutoPilot(shipId);
      return ResponseEntity.ok(result);
    } catch (IllegalArgumentException e) {
      log.warn("AutoPilot failed - invalid ship: {} → {}", shipId, e.getMessage());
      return ResponseEntity.notFound().build();
    } catch (IllegalStateException e) {
      log.error("AutoPilot execution error for ship {}: {}", shipId, e.getMessage());
      return ResponseEntity.internalServerError().build();
    } catch (Exception e) {
      log.error("Unexpected error in autoPilot for ship {}", shipId, e);
      return ResponseEntity.internalServerError().build();
    }
  }
}








