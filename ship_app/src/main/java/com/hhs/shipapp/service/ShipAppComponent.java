package com.hhs.shipapp.service;

import com.hhs.lib.model.*;
import com.hhs.shipapp.models.Echo;
import com.hhs.shipapp.models.RadarResponse;
import com.hhs.shipapp.models.ShipEntityState;
import com.hhs.shipapp.models.ShipMessage;
import com.hhs.shipapp.models.enums.Course;
import com.hhs.shipapp.models.enums.Rudder;
import com.hhs.shipapp.util.Helper;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Getter
@Setter
@Component
public class ShipAppComponent {

  private final Map<String, ShipEntityState> shipEntityStateMap;
  private final ShipTransportMessage shipTransportMessage;

  private String shipId;

  private Vec2D shipSector;
  private Vec2D shipDirection;
  private Vec2D newGoalShipSectorAfterBlocking;

  private List<ShipMessage> shipMessages;
  private List<Echo> echoList = new ArrayList<>();
  private List<Vec2D> allowedShipSurroundingSectors;

  private Set<Vec2D> navigableDirections;
  private Set<Sector> notNavigableDirections;

  private RadarResponse radarResponse;

  private Optional<Vec2D> shipBlockingSector;

  private final Logger log = LoggerFactory.getLogger(ShipAppComponent.class);


  public ShipAppComponent(ShipTransportMessage shipTransportMessage, Map<String, ShipEntityState> shipEntityStateMap) {
    this.shipTransportMessage = shipTransportMessage;
    this.shipEntityStateMap = shipEntityStateMap;
  }

  public void updateShipSectorAndDirection() {
    ShipMessage shipMessage = shipMessages.getFirst();

    shipSector = new Vec2D(shipMessage.getSector().getVec2()[0], shipMessage.getSector().getVec2()[1]);
    shipDirection = new Vec2D(shipMessage.getDir().getVec2()[0], shipMessage.getDir().getVec2()[1]);
  }

  public void fillShipEntityStateMap() {
    ShipEntityState state = new ShipEntityState();
    state.setSector(shipSector);
    state.setDirection(shipDirection);

    shipEntityStateMap.put(shipId, state);
  }

  public RadarResponse getRadarResponse() {
    List<Sector> verboteneRichtungen = new ArrayList<>();
    RadarResponse radarResponse = new RadarResponse();

    echoList = shipMessages.getFirst().getEchos();

    radarResponse.setEchos(echoList);

    for (Echo echo : echoList) {
      Vec2D orientation = new Vec2D(echo.getSector().getVec2()[0] - shipSector.getX(),
                                    echo.getSector().getVec2()[1] - shipSector.getY());

      if (!isSectorNavigable(echo)) {
        verboteneRichtungen.add(new Sector(orientation));
      }
    }

    radarResponse.setNotNavigable(verboteneRichtungen);     // orientation z.B.: [0,-1]

    return radarResponse;
  }

  private boolean isSectorNavigable(Echo echo) {
    return echo.getHeight() <= 0 && (echo.getGround() == Ground.Water || echo.getGround() == Ground.Harbour);
  }

  public void persistSectorData() {
    echoList.forEach(echo -> {
      SectorData sectorData = new SectorData();
      sectorData.setShipId(shipId);
      sectorData.setGround(echo.getGround());
      sectorData.setHeight(echo.getHeight());
      sectorData.setSectorX(echo.getSector().getVec2()[0]);
      sectorData.setSectorY(echo.getSector().getVec2()[1]);

      shipTransportMessage.saveSectorData(sectorData);
    });
  }

  public void updateSectorData() {
    SectorData sectorData = new SectorData();
    sectorData.setShipId(shipId);
    sectorData.setSectorX(shipSector.getX());
    sectorData.setSectorY(shipSector.getY());
    sectorData.setDepth(shipMessages.getFirst().getDepth());
    sectorData.setStddev(shipMessages.getFirst().getStddev());

    log.info("SectorData updated | shipId={} | sectorX={} | sectorY={} | depth={} | stddev={}", sectorData.getShipId(),
             sectorData.getSectorX(), sectorData.getSectorY(), sectorData.getDepth(), sectorData.getStddev());

    shipTransportMessage.updateSectorData(sectorData);
  }

  public void updateShipEntityState(String course, String rudder) {
    Vec2D newShipSector = new Vec2D(shipMessages.getFirst().getSector().getVec2()[0],
                                    shipMessages.getFirst().getSector().getVec2()[1]);
    Vec2D newShipDirection = new Vec2D(shipMessages.getFirst().getDir().getVec2()[0],
                                       shipMessages.getFirst().getDir().getVec2()[1]);

    shipSector = newShipSector;
    shipDirection = newShipDirection;

    ShipEntityState state = shipEntityStateMap.get(shipId);

    Course parsedCourse = Course.fromString(course);
    Rudder parsedRudder = Rudder.fromString(rudder);

    state.setPreviousSector(state.getSector());
    state.setSector(shipSector);
    state.setDirection(shipDirection);
    state.setCourse(parsedCourse);
    state.setRudder(parsedRudder);

    log.info("ShipEntityState updated to: " + state);

    state.recordMove(state.getSector(), parsedCourse, parsedRudder);
  }

  public Ship fetchUpdatedShipData() {
    Ship updatedShip = new Ship();
    updatedShip.setShipId(shipId);
    updatedShip.setShipName(Helper.extractShipNameFromShipId(shipId));
    updatedShip.setSectorX(shipSector.getX());
    updatedShip.setSectorY(shipSector.getY());
    updatedShip.setDirectionX(shipDirection.getX());
    updatedShip.setDirectionY(shipDirection.getY());

    return updatedShip;
  }

  public void removeShipEntityState(String shipId) {
    shipEntityStateMap.remove(shipId);
  }

  /* methods for ship Route */
  public void saveShipSector() {
    ShipSector shipSector = new ShipSector();
    shipSector.setShipSectorX(this.shipSector.getX());
    shipSector.setShipSectorY(this.shipSector.getY());

    shipTransportMessage.saveShipSector(shipSector);
  }



  /* aoto pilot*/
  public void calcAllowedSurroundingSectors() {
    allowedShipSurroundingSectors = new ArrayList<>();

    calcNavigableDirections();

    for (Vec2D vec2D : navigableDirections) {
      allowedShipSurroundingSectors.add(new Vec2D(shipSector.getX() + vec2D.getX(), shipSector.getY() + vec2D.getY()));
    }

    log.info("allowedSurroundingFields: " + allowedShipSurroundingSectors);
  }

  private void calcNavigableDirections() {
    RelativeCoordinateSystem relativeCoordinateSystem = new RelativeCoordinateSystem(shipDirection);

    navigableDirections = new HashSet<>();

    // alle blockierten Richtungen sammeln
    Set<Vec2D> blocked = new HashSet<>();

    for (Sector sector : radarResponse.getNotNavigable()) {
      blocked.add(new Vec2D(sector.getVec2()[0], sector.getVec2()[1]));
    }

    for (Vec2D dir : relativeCoordinateSystem.getCoordinates()) {
      if (!blocked.contains(dir)) {
        navigableDirections.add(dir);
      }
    }
  }


  public Optional<Vec2D> findShipBlockingSector() {
    Vec2D nextSector = new Vec2D(shipSector.getX() + shipDirection.getX(), shipSector.getY() + shipDirection.getY());

    shipBlockingSector = radarResponse.getEchos().stream().filter(
                                          echo -> echo.getGround() != Ground.Harbour && echo.getGround() != Ground.Water).map(Echo::getSector)
                                      .map(Sector::getVec2)
                                      .filter(vec -> vec[0] == nextSector.getX() && vec[1] == nextSector.getY())
                                      .map(vec -> new Vec2D(vec[0], vec[1])).findFirst();

    if (shipBlockingSector.isPresent()) {
      calcNewGoalShipSectorAfterBlocking();
    }

    return shipBlockingSector;
  }

  private void calcNewGoalShipSectorAfterBlocking() {
    newGoalShipSectorAfterBlocking = new Vec2D(shipSector.getX() + shipDirection.getX() + shipDirection.getX(),
                                               shipSector.getY() + shipDirection.getY() + shipDirection.getY());

    log.info("nextAllowedShipSector: " + newGoalShipSectorAfterBlocking);
  }

  private void calcNotNavigableDirections() {
    notNavigableDirections = new HashSet<>(radarResponse.getNotNavigable());

    RelativeCoordinateSystem relCoord = new RelativeCoordinateSystem(shipDirection);

    notNavigableDirections.add(new Sector(relCoord.getCoordinates().get(2))); // east
    notNavigableDirections.add(new Sector(relCoord.getCoordinates().get(6))); // west

  }

  public void calcRouteTree(List<Sector> notNavigable) {
    for (Vec2D vec2D : navigableDirections) {
      allowedShipSurroundingSectors.add(new Vec2D(shipSector.getX() + vec2D.getX(), shipSector.getY() + vec2D.getY()));
    }

    for (int i = 0; i < allowedShipSurroundingSectors.size(); i++) {
      Route route = new Route();
      Vec2D v = allowedShipSurroundingSectors.get(i);
      addRoute(route, List.of("01", "12", "23"), List.of(new Vec2D(v.getX(), v.getY()), new Vec2D(v.getX(), v.getY())));
    }

  }

  public void addRoute(Route route, List<String> path, List<Vec2D> routePoints) {

    Map<String, RouteNode> map = route.getRoute();
    RouteNode node = null;

    for (String key : path) {
      node = map.computeIfAbsent(key, k -> new RouteNode());
      map = node.getChildren();
    }

    node.getRouteList().addAll(routePoints);
  }


}
