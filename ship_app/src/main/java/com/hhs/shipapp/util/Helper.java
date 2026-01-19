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

  public static Vec2D updateSectorAtShipPosition(List<ShipMessage> messages) {
    Vec2D vec2D = new Vec2D(messages.getFirst().getSector().getVec2()[0], messages.getFirst().getSector().getVec2()[1]);
    return vec2D;
  }

}













