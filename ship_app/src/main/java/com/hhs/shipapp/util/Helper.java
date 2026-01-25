package com.hhs.shipapp.util;

import com.hhs.lib.model.*;
import com.hhs.shipapp.models.Echo;
import com.hhs.shipapp.models.ShipEntityState;
import com.hhs.shipapp.models.ShipMessage;
import com.hhs.shipapp.models.enums.Course;
import com.hhs.shipapp.models.enums.Rudder;
import com.hhs.shipapp.models.messages.Launched;
import com.hhs.shipapp.models.messages.RadarResponse;
import com.hhs.shipapp.service.ShipTransportMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  public static RadarResponse getRadarResponse(ShipMessage shipMessage, Vec2D sectorAtShipPosition,
                                               Vec2D shipDirection) {
    List<Sector> verboteneRichtungen = new ArrayList<>();
    RadarResponse radarResponse = new RadarResponse();
    radarResponse.setEchos(shipMessage.getEchos());

    for (Echo echo : shipMessage.getEchos()) {
      Vec2D orientation = new Vec2D(echo.getSector().getVec2()[0] - sectorAtShipPosition.getX(), echo.getSector()
                                                                                                     .getVec2()[1] - sectorAtShipPosition.getY()); // orientation z.B.: [0,-1]

      if (!isSectorNavigable(echo)) {
        verboteneRichtungen.add(new Sector(orientation));
      }
    }

    radarResponse.setNotNavigable(verboteneRichtungen);

    return radarResponse;
  }

  public static void persistSectorData(String shipId, ShipTransportMessage shipTransportMessage,
                                       List<ShipMessage> shipMessages) {
    shipMessages.getFirst().getEchos().forEach(echo -> {
      SectorData sectorData = new SectorData();
      sectorData.setShipId(shipId);
      sectorData.setGround(echo.getGround());
      sectorData.setHeight(echo.getHeight());
      sectorData.setSectorX(echo.getSector().getVec2()[0]);
      sectorData.setSectorY(echo.getSector().getVec2()[1]);

      shipTransportMessage.saveSectorData(sectorData);
    });

  }

  private static boolean isSectorNavigable(Echo echo) {
    return echo.getHeight() <= 0 && (echo.getGround() == Ground.Water || echo.getGround() == Ground.Harbour);
  }

  public static void updateShipEntityState(Map<String, ShipEntityState> shipEntityStateMap, String shipId,
                                           List<ShipMessage> shipMessages, String course, String rudder) {
    ShipEntityState state = shipEntityStateMap.get(shipId);

    Course parsedCourse = Course.fromString(course);
    Rudder parsedRudder = Rudder.fromString(rudder);

    state.setDirection(Helper.updateShipDirection(shipMessages));
    state.setSector(Helper.updateSectorAtShipPosition(shipMessages));
    state.setCourse(parsedCourse);
    state.setRudder(parsedRudder);

    state.recordMove(state.getSector(), parsedCourse, parsedRudder);
  }

  public static ShipEntityState updateShipEntityStateMap(String shipId, String name, Vec2D sector, Vec2D direction) {
    ShipEntityState state = new ShipEntityState();
    state.setShipId(shipId);
    state.setName(name);
    state.setSector(sector);
    state.setDirection(direction);

    return state;
  }

  public static String extractShipNameFromShipId(String shipId) {
    Pattern pattern = Pattern.compile("#\\d+#(.*)");
    Matcher matcher = pattern.matcher(shipId);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }

  public static void updateSectorData(String shipId, ShipTransportMessage shipTransportMessage,
                                      List<ShipMessage> shipMessages) {
    ShipData shipData = shipTransportMessage.getShipData(shipId);

    Sector sector = new Sector(new Vec2D(shipData.getSectorX(), shipData.getSectorY()));

    //TODO find sector in DB, than update it, if it exists
    SectorData sectorData = new SectorData();
    sectorData.setShipId(shipId);
    sectorData.setSectorX(sector.getVec2()[0]);
    sectorData.setSectorY(sector.getVec2()[1]);
    sectorData.setDepth(shipMessages.getFirst().getDepth());
    sectorData.setStddev(shipMessages.getFirst().getStddev());

    System.out.println(sectorData);
    shipTransportMessage.updateSectorData(sectorData);
  }

  public static void deleteShip(String shipId, ShipTransportMessage shipTransportMessage) {
     }
}













