package com.hhs.shipapp.controller;

import com.hhs.lib.model.Vec2D;
import com.hhs.shipapp.models.NavigableOrientation;
import com.hhs.shipapp.models.ShipMessage;
import com.hhs.shipapp.models.enums.NavigableDirections;
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
public class ShipController {

    private final ShipAppImpl shipAppImpl;
    private final ShipTransportMessage shipTransportMessage;
    private NavigableDirections navigableOrientation;
    private Vec2D sectorAtShipPosition;
    private Vec2D shipDirection;

    public ShipController(ShipAppImpl shipAppImpl, ShipTransportMessage shipTransportMessage) {
        this.shipAppImpl = shipAppImpl;
        this.shipTransportMessage = shipTransportMessage;
    }

    @PostMapping("/launch")
    public ResponseEntity<String> launch(@RequestParam String name, @RequestParam int x, @RequestParam int y,
                                                    @RequestParam int dx, @RequestParam int dy) {

        sectorAtShipPosition = new Vec2D(x, y);
        shipDirection = new Vec2D(dx, dy);


        List<ShipMessage> shipMessages = shipAppImpl.launch(name, sectorAtShipPosition, shipDirection);

        System.out.println(shipMessages.getFirst());
        System.out.println(shipMessages.getLast());

        return ResponseEntity.ok("Success");
    }

    @PostMapping("/navigate")
    public ResponseEntity<List<ShipMessage>> navigate(@RequestParam String shipId, @RequestParam String course, @RequestParam String rudder) {

        List<ShipMessage> shipMessages = shipAppImpl.navigate(course, rudder);

        String navigate = course + rudder;
        String navigation = NavigableDirections.fromString(navigate);

        shipDirection = Helper.updateShipDirection(shipMessages);
        sectorAtShipPosition = Helper.updateSectorAtShipPosition(sectorAtShipPosition, shipDirection, navigation);

        return ResponseEntity.ok(shipMessages);
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
    public ResponseEntity<NavigableOrientation> radar() {
        List<ShipMessage> messages = shipAppImpl.radar();
//    shipTransportMessage.sendMessage();

        NavigableOrientation navigableOrientation = NavigableOrientation.getNavigableOrientation(messages.getFirst(),
                                                                                                 sectorAtShipPosition);
        return ResponseEntity.ok(navigableOrientation);
    }

}

















