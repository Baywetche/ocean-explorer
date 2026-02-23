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
import org.springframework.stereotype.Service;

import java.util.*;

@Getter
@Setter
@Service
public class ShipAppComponent {

  private String shipId;

  private Vec2D shipSector;
  private Vec2D shipDirection;
  private Vec2D shipGoalDirection;
  private Vec2D newGoalShipSectorAfterBlocking;

  private List<ShipMessage> shipMessages;
  private List<Echo> echoList = new ArrayList<>();
  private List<Vec2D> allowedShipSurroundingSectors;

  private Set<Vec2D> navigableDirections;
  private Optional<Vec2D> shipBlockingSector;

  private final Map<String, ShipEntityState> shipEntityStateMap;
  private final ShipTransportMessage shipTransportMessage;
  private final Logger log = LoggerFactory.getLogger(ShipAppComponent.class);
  private final Map<String, Boolean> isShipMakingCircularMovementMap = new HashMap<>();

  private RadarResponse radarResponse;
  private ShipStraightOnDirection driveableShipGoalDirection;
  private RelativeCoordinateSystem relativeCoordinateSystem = new RelativeCoordinateSystem(shipSector);


  public ShipAppComponent(ShipTransportMessage shipTransportMessage, Map<String, ShipEntityState> shipEntityStateMap) {
    this.shipTransportMessage = shipTransportMessage;
    this.shipEntityStateMap = shipEntityStateMap;
  }

  public void updateShipSectorAndDirectionAndCoordinateSystem() {
    ShipMessage shipMessage = shipMessages.getFirst();

    shipSector = new Vec2D(shipMessage.getSector().getVec2()[0], shipMessage.getSector().getVec2()[1]);
    shipDirection = new Vec2D(shipMessage.getDir().getVec2()[0], shipMessage.getDir().getVec2()[1]);

    relativeCoordinateSystem = new RelativeCoordinateSystem(shipDirection);
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

  // ===== ship Route =====
  public boolean saveShipSector() {
    ShipSector shipSector = new ShipSector();
    shipSector.setShipId(shipId);
    shipSector.setShipSectorX(this.shipSector.getX());
    shipSector.setShipSectorY(this.shipSector.getY());

    return shipTransportMessage.saveShipSector(shipSector);
  }

  // ===== Autopilot =====
  public void updateDriveableShipGoalDirection() {
    if (shipDirection.equals(shipGoalDirection)) {
      driveableShipGoalDirection = ShipStraightOnDirection.Forward;
    }
    else if (shipDirection.getX() == -shipGoalDirection.getX() && shipDirection.getY() == -shipGoalDirection.getY()) {
      driveableShipGoalDirection = ShipStraightOnDirection.Backward;
    }
    else {
      driveableShipGoalDirection = ShipStraightOnDirection.Disabled;
    }
  }

  public boolean isShipBlocked() {
    return findShipBlockingSector().isPresent();
  }

  private Optional<Vec2D> findShipBlockingSector() {
    Vec2D nextSector = new Vec2D(shipSector.getX() + shipDirection.getX(), shipSector.getY() + shipDirection.getY());

    shipBlockingSector = radarResponse.getEchos().stream().filter(
                                          echo -> echo.getGround() != Ground.Harbour && echo.getGround() != Ground.Water).map(Echo::getSector)
                                      .map(Sector::getVec2)
                                      .filter(vec -> vec[0] == nextSector.getX() && vec[1] == nextSector.getY())
                                      .map(vec -> new Vec2D(vec[0], vec[1])).findFirst();

    return shipBlockingSector;
  }

  public boolean driveableWithCommand(String course, String rudder) {
    String drive2 = course + "_" + rudder;

    Vec2D direction = switch (drive2) {
      case "Forward_Center" -> relativeCoordinateSystem.getCoordinates().get(0);
      case "Forward_Right" -> relativeCoordinateSystem.getCoordinates().get(1);
      case "Backward_Right" -> relativeCoordinateSystem.getCoordinates().get(3);
      case "Backward_Center" -> relativeCoordinateSystem.getCoordinates().get(4);
      case "Backward_Left" -> relativeCoordinateSystem.getCoordinates().get(5);
      case "Forward_Left" -> relativeCoordinateSystem.getCoordinates().get(7);

      default -> throw new IllegalStateException("Unexpected value: " + drive2);
    };

 /*   System.out.println("shipDirection: " + shipDirection);
    System.out.println("direction: " + direction);
    System.out.println("navigableDirection: " + navigableDirections);
*/
    return navigableDirections.contains(direction);
  }

  public boolean isShipAtSouthBoundary() {
    return shipSector.getY() == 0;
  }

  public boolean isShipAtWestBoundary() {
    return shipSector.getX() == 0;
  }

  public boolean isShipAtNorthBoundary() {
    return shipSector.getY() == 99;
  }

  public void calculateNavigableDirections() {
    navigableDirections = new HashSet<>();

    // alle blockierten Richtungen sammeln
    Set<Vec2D> blocked = new HashSet<>(updateNotNavigableDirections());

    for (Vec2D dir : relativeCoordinateSystem.getCoordinates()) {
      if (!blocked.contains(dir)) {
        navigableDirections.add(dir);
      }
    }
  }

  private List<Vec2D> updateNotNavigableDirections() {
    Set<Sector> notNavigableDirections = new HashSet<>(radarResponse.getNotNavigable());

    notNavigableDirections.add(new Sector(relativeCoordinateSystem.getCoordinates().get(2))); // east
    notNavigableDirections.add(new Sector(relativeCoordinateSystem.getCoordinates().get(6))); // west

    List<Vec2D> forbiddenDirections = new ArrayList<>();
    for (Sector sector : notNavigableDirections) {
      forbiddenDirections.add(new Vec2D(sector.getVec2()[0], sector.getVec2()[1]));
    }

    return forbiddenDirections;
  }

  public AutoPilotData buildAutoPilotData() {
    AutoPilotData autoPilotData = new AutoPilotData();

    autoPilotData.setShipId(shipId);
    autoPilotData.setShipPosition(new ShipPosition(shipSector.getX(), shipSector.getY()));
    autoPilotData.setSectorDataList(buildSectorDataList());

    return autoPilotData;
  }

  private List<SectorData> buildSectorDataList() {
    List<SectorData> sectorDataList = new ArrayList<>();

    echoList.forEach(echo -> {
      SectorData sectorData = new SectorData();

      sectorData.setShipId(shipId);
      sectorData.setGround(echo.getGround());
      sectorData.setHeight(echo.getHeight());
      sectorData.setSectorX(echo.getSector().getVec2()[0]);
      sectorData.setSectorY(echo.getSector().getVec2()[1]);

      sectorDataList.add(sectorData);
    });

    return sectorDataList;
  }



}
