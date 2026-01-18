package com.hhs.shipapp.util;

import com.hhs.lib.model.RelativeCoordinateSystem;
import com.hhs.lib.model.Vec2D;
import com.hhs.shipapp.models.*;

import java.util.List;

public class Helper {


    public static Vec2D updateShipDirection(List<ShipMessage> shipMessages) {
        return new Vec2D(shipMessages.getFirst().getDir().getVec2()[0],
                         shipMessages.getFirst().getDir().getVec2()[1]);
    }

    public static Vec2D updateSectorAtShipPosition(Vec2D sectorAtShipPosition, Vec2D dir, String navigateString) {

        RelativeCoordinateSystem rcs = new RelativeCoordinateSystem(dir);

        Vec2D moved = switch (navigateString) {
            case "n" -> rcs.getCoordinates().getFirst();
            case "ne" -> rcs.getCoordinates().get(1);
            case "e" -> rcs.getCoordinates().get(2);
            case "se" -> rcs.getCoordinates().get(3);
            case "s" -> rcs.getCoordinates().get(4);
            case "sw" -> rcs.getCoordinates().get(5);
            case "w" -> rcs.getCoordinates().get(6);
            case "nw" -> rcs.getCoordinates().getLast();
            default -> throw new IllegalStateException("Unexpected moveable direction: " + navigateString);
        };

        return new Vec2D(sectorAtShipPosition.getX() + moved.getX(),
                         sectorAtShipPosition.getY() + moved.getY());
    }


}













