package com.hhs.shipapp.util;

import com.hhs.lib.model.*;
import com.hhs.shipapp.models.Echo;
import com.hhs.shipapp.models.ShipEntityState;
import com.hhs.shipapp.models.ShipMessage;
import com.hhs.shipapp.models.enums.Commands;
import com.hhs.shipapp.models.enums.Course;
import com.hhs.shipapp.models.enums.Rudder;
import com.hhs.shipapp.models.messages.Launched;
import com.hhs.shipapp.models.messages.RadarResponse;
import com.hhs.shipapp.service.ShipTransportMessage;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Helper {

  public static Vec2D getShipDirection(List<ShipMessage> shipMessages) {
    return new Vec2D(shipMessages.getFirst().getDir().getVec2()[0], shipMessages.getFirst().getDir().getVec2()[1]);
  }

  public static Vec2D getShipSector(List<ShipMessage> messages) {
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

  public static void persistSectorData(String shipId, ShipTransportMessage shipTransportMessage, List<ShipMessage> shipMessages) {
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

  public static void updateShipEntityState(Map<String, ShipEntityState> shipEntityStateMap, String shipId, List<ShipMessage> shipMessages,
      String course, String rudder) {

    ShipEntityState state = shipEntityStateMap.get(shipId);

    Course parsedCourse = Course.fromString(course);
    Rudder parsedRudder = Rudder.fromString(rudder);

    state.setPreviousSector(state.getSector());
    state.setSector(Helper.getShipSector(shipMessages));
    state.setDirection(Helper.getShipDirection(shipMessages));
    state.setCourse(parsedCourse);
    state.setRudder(parsedRudder);

    System.out.println("state: " + state);

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

  public static void updateSectorData(String shipId, ShipTransportMessage shipTransportMessage, List<ShipMessage> shipMessages) {
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

  public static Vec2D calculateTargetDirection(ShipEntityState shipEntityStateMap, Vec2D goalSector) {
    Vec2D sector = shipEntityStateMap.getSector();

    int targetXSign = Integer.signum(goalSector.getX() - sector.getX());
    int targetYSign = Integer.signum(goalSector.getY() - sector.getY());

    return new Vec2D(targetXSign, targetYSign);
  }

  public static ShipMessage getShipMessageFromGivenShipOrientationAndTargetDirection(Vec2D currentOrientation, Vec2D targetDirection,
      List<Sector> notNavigable) {
    RelativeCoordinateSystem relativeCoordinateSystem = new RelativeCoordinateSystem(currentOrientation);

    List<Vec2D> allowedDirections = new ArrayList<>(relativeCoordinateSystem.getCoordinates());

    // build a Vec2D-Set of not allowed directions
    Set<Vec2D> forbiddenDirections = notNavigable.stream().map(s -> new Vec2D(s.getVec2()[0], s.getVec2()[1])).collect(Collectors.toSet());

    // Entferne alle verbotenen Richtungen aus der Liste, sodass nur die gültigen/möglichen übrig bleiben
    allowedDirections.removeAll(forbiddenDirections);

    int index = allowedDirections.indexOf(targetDirection);

    return switch (index) {
      case 0 -> ShipMessage.builder().cmd(Commands.navigate).course(Course.Forward).rudder(Rudder.Center).build(); // north
      case 1 -> ShipMessage.builder().cmd(Commands.navigate).course(Course.Forward).rudder(Rudder.Right).build(); // northEsat
      //      case 2 -> east -> this is not allowed
      case 3 -> ShipMessage.builder().cmd(Commands.navigate).course(Course.Backward).rudder(Rudder.Left).build(); // southEast
      case 4 -> ShipMessage.builder().cmd(Commands.navigate).course(Course.Backward).rudder(Rudder.Center).build(); // south
      case 5 -> ShipMessage.builder().cmd(Commands.navigate).course(Course.Backward).rudder(Rudder.Right).build(); // southWest
      //      case 6 -> west -> this is not allowed
      case 7 -> ShipMessage.builder().cmd(Commands.navigate).course(Course.Forward).rudder(Rudder.Left).build(); // northWest

      default -> throw new IllegalStateException("Unexpected orientation: " + index);
    };
  }

  public static Optional<Vec2D> getShipBlockingSector(ShipEntityState shipEntityStateMap, RadarResponse radarResponse) {
    Vec2D direction = shipEntityStateMap.getDirection();
    Vec2D shipSector = shipEntityStateMap.getSector();

    List<Sector> blockedSectors =
        radarResponse.getEchos().stream().filter(echo -> echo.getGround() != Ground.Harbour && echo.getGround() != Ground.Water)
            .map(echo -> echo.getSector()).toList();

    Vec2D nextPossiblyShipSector = new Vec2D(shipSector.getX() + direction.getX(), shipSector.getY() + direction.getY());

    Optional<Vec2D> shipBlockingSector = blockedSectors.stream()
        .filter(sector -> sector.getVec2()[0] == nextPossiblyShipSector.getX() && sector.getVec2()[1] == nextPossiblyShipSector.getY())
        .map(sector -> new Vec2D(sector.getVec2()[0], sector.getVec2()[1])).findFirst();

    System.out.println("shipBlockingSector: " + shipBlockingSector);
    return shipBlockingSector;
  }

  public static Vec2D calcNewGoalShipSectorAfterBlocking(ShipEntityState shipEntityStateMap) {
    Vec2D shipSector = shipEntityStateMap.getSector();
    Vec2D direction = shipEntityStateMap.getDirection();

    Vec2D nextAllowedShipSector =
        new Vec2D(shipSector.getX() + direction.getX() + direction.getX(), shipSector.getY() + direction.getY() + direction.getY());

    System.out.println("nextAllowedShipSector: " + nextAllowedShipSector);
    return nextAllowedShipSector;
  }

  public static List<Vec2D> calcAllowedSurroundingFields(ShipEntityState shipEntityStateMap, List<Sector> notNavigable) {
    Vec2D direction = shipEntityStateMap.getDirection();
    List<Vec2D> navigableDirections = getNavigableSectors(direction, notNavigable);

    Vec2D shipSector = shipEntityStateMap.getSector();

    List<Vec2D> allowedSurroundingFields = new ArrayList<>();

    for (Vec2D vec2D : navigableDirections) {
      allowedSurroundingFields.add(new Vec2D(shipSector.getX() + vec2D.getX(), shipSector.getY() + vec2D.getY()));
    }

    System.out.println("allowedSurroundingFields: " + allowedSurroundingFields);

    return allowedSurroundingFields;
  }

  private static List<Vec2D> getNavigableSectors(Vec2D direction, List<Sector> sectors) {
    RelativeCoordinateSystem relativeCoordinateSystem = new RelativeCoordinateSystem(direction);

    // alle blockierten Richtungen sammeln
    Set<Vec2D> blocked = new HashSet<>();

    for (Sector sector : sectors) {
      blocked.add(new Vec2D(sector.getVec2()[0], sector.getVec2()[1]));
    }

    List<Vec2D> navigableDir = new ArrayList<>();

    for (Vec2D dir : relativeCoordinateSystem.getCoordinates()) {
      if (!blocked.contains(dir)) {
        navigableDir.add(dir);
      }
    }

    return navigableDir;
  }

  public static RoutePlan createRoutePlan(ShipEntityState shipEntityStateMap, Vec2D surroundSector, Vec2D newGoalShipSectorAfterBlocking) {
    RelativeCoordinateSystem relativeCoordinateSystem = new RelativeCoordinateSystem(shipEntityStateMap.getDirection());
    Vec2D east = relativeCoordinateSystem.getCoordinates().get(2);
    Vec2D west = relativeCoordinateSystem.getCoordinates().get(6);

    RoutePlan routePlan = new RoutePlan();
    routePlan.addStep(0, 0);

    List<Vec2D> level1 = new ArrayList<>();

    for (Vec2D vec2D : relativeCoordinateSystem.getCoordinates()) {
      if (vec2D.equals(east) || vec2D.equals(west)) {
        continue;
      }

      Vec2D sector = new Vec2D(surroundSector.getX() + vec2D.getX(), surroundSector.getY() + vec2D.getY());
      if (surroundSector.equals(sector)) {
        continue;
      }
      level1.add(vec2D);

    }

    return routePlan;
  }

  public static void getRouteTree(ShipEntityState shipEntityStateMap, List<Sector> notNavigable) {
    Vec2D direction = shipEntityStateMap.getDirection();
    List<Vec2D> navigableDirections = getNavigableSectors(direction, notNavigable);

    Vec2D shipSector = shipEntityStateMap.getSector();

    List<Vec2D> allowedSurroundingFields = new ArrayList<>();

    for (Vec2D vec2D : navigableDirections) {
      allowedSurroundingFields.add(new Vec2D(shipSector.getX() + vec2D.getX(), shipSector.getY() + vec2D.getY()));
    }

    for (int i = 0; i < allowedSurroundingFields.size(); i++) {
      Route route = new Route();
      Vec2D v = allowedSurroundingFields.get(i);
      addRoute(route, List.of("01", "12", "23"), List.of(new Vec2D(v.getX(), v.getY()), new Vec2D(v.getX(),v.getY())));

    }

  }

  public static void addRoute(Route route, List<String> path, List<Vec2D> routePoints) {

    Map<String, RouteNode> map = route.getRoute();
    RouteNode node = null;

    for (String key : path) {
      node = map.computeIfAbsent(key, k -> new RouteNode());
      map = node.getChildren();
    }

    node.getRouteList().addAll(routePoints);
  }





























  /*private static void fillRouteTree(List<Vec2D> navigableSectors) {
    List<String> path = new ArrayList<>();
    Route route = new Route();
    for (Vec2D vec2D: navigableSectors){
      path.add(vec2D.getX() + String.valueOf(vec2D.getY()));

      Map<String, RouteNode> currentMap = route.getRoute();

      RouteNode currentNode = null;

      for (String key : path) {
        currentNode = currentMap.get(key);

        if (currentNode == null) {
          currentNode = new RouteNode();
          currentMap.put(key, currentNode);
        }

        // für den nächsten Durchlauf:
        currentMap = currentNode.getChildren();
      }

      route.getRoute().put(vec2D + String.valueOf(vec2D.getY()), currentNode);

      currentNode.getRouteList().add(new Vec2D(vec2D.getX(),  vec2D.getY()));
      currentNode.getRouteList().add(new Vec2D(vec2D.getX(),  vec2D.getY()));

    }

    System.out.println(route);
  }*/

}

























