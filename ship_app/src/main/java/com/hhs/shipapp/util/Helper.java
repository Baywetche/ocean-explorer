package com.hhs.shipapp.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhs.lib.model.Ground;
import com.hhs.lib.model.RelativeCoordinateSystem;
import com.hhs.lib.model.Sector;
import com.hhs.lib.model.Vec2D;
import com.hhs.shipapp.models.*;
import com.hhs.shipapp.models.messages.Launched;
import com.hhs.shipapp.models.messages.RadarResponse;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Helper {

  public static Vec2D updateShipDirection(List<ShipMessage> shipMessages) {
    return new Vec2D(shipMessages.getFirst().getDir().getVec2()[0], shipMessages.getFirst().getDir().getVec2()[1]);
  }

  public static Vec2D updateSectorAtShipPosition(List<ShipMessage> messages) {
    Vec2D vec2D = new Vec2D(messages.getFirst().getSector().getVec2()[0], messages.getFirst().getSector().getVec2()[1]);
    return vec2D;
  }

  public static Launched getLaunched(List<ShipMessage> shipMessages) {
    Launched launched = new Launched();
    launched.setCmd(shipMessages.getFirst().getCmd());
    launched.setId(shipMessages.getFirst().getId());
    launched.setAbspos(shipMessages.getFirst().getAbspos());

    return launched;
  }

  public static RadarResponse getRadarResponse(ShipMessage shipMessage, Vec2D sectorAtShipPosition, Vec2D shipDirection) {
    List<Sector> verboteneRichtungen = new ArrayList<>();
    RadarResponse radarResponse = new RadarResponse();
    radarResponse.setEchos(shipMessage.getEchos());

    for (Echo echo : shipMessage.getEchos()) {
      Vec2D orientation = new Vec2D(echo.getSector().getVec2()[0] - sectorAtShipPosition.getX(),
          echo.getSector().getVec2()[1] - sectorAtShipPosition.getY()); // orientation z.B.: [0,-1]

      if (!isSectorNavigable(echo)) {
        verboteneRichtungen.add(new Sector(orientation));
      }
    }

    radarResponse.setNotNavigable(verboteneRichtungen);

    return radarResponse;
  }

  private static boolean isSectorNavigable(Echo echo) {
    return echo.getHeight() <= 0 && (echo.getGround() == Ground.Water || echo.getGround() == Ground.Harbour);
  }

}













