package com.hhs.shipbaseserver.controller;

import com.hhs.lib.model.RoutePlan;
import com.hhs.lib.model.SectorData;
import com.hhs.lib.model.Ship;
import com.hhs.lib.model.ShipSector;
import com.hhs.shipbaseserver.dao.RoutePlanService;
import com.hhs.shipbaseserver.dao.SectorDataService;
import com.hhs.shipbaseserver.dao.ShipService;
import com.hhs.shipbaseserver.dao.ShipRouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shipBaseServerAPI")
public class ShipBaseServerController {

  private final SectorDataService sectorDataService;
  private final ShipService shipService;
  private final RoutePlanService routePlanService;
  private final ShipRouteService shipRouteService;

  public ShipBaseServerController(SectorDataService sectorDataService, ShipService shipService,
                                  RoutePlanService routePlanService, ShipRouteService shipRouteService) {
    this.sectorDataService = sectorDataService;
    this.shipService = shipService;
    this.routePlanService = routePlanService;
    this.shipRouteService = shipRouteService;
  }

  /**
   * Saves the given sector information into the database.
   *
   * @param sectorData the sector information to be saved
   * @return {@code true} if the sector information was successfully saved, {@code false} otherwise
   */
  @PostMapping(value = "/saveSectorData")
  public ResponseEntity<Boolean> saveSectorData(@RequestBody SectorData sectorData) {
    //    System.out.println("Empfangen: " + sectorData.toString());

    return sectorDataService.save(sectorData);
  }

  @PutMapping(value = "/updateSectorData")
  public ResponseEntity<Boolean> updateSectorData(@RequestBody SectorData sectorData) {
    //    System.out.println("Empfangen: " + sectorData.toString());

    return sectorDataService.update(sectorData);
  }

  /**
   * Checks whether the specified sector exists in the database table.
   *
   * @return {@code true} if the sector exists, otherwise {@code false}
   */
  @GetMapping(value = "/findSectorData") //TODO check, if works correctly
  public ResponseEntity<SectorData> findSectorData(@RequestParam int sectorX, @RequestParam int sectorY) {
    System.out.println("findSectorData for: (" + sectorX + ", " + sectorY + ")");

    return sectorDataService.findBySector(sectorX, sectorY);
  }

  /**
   * Retrieves all sector information entries from the database.
   *
   * @return a list of all {@link SectorData} objects
   */
  @GetMapping(value = "/allSectorData") //TODO check, if works correctly
  public List<SectorData> findAllSectorData() {
    return sectorDataService.getAllSectorData();
  }

  /**
   * Saves the given ship data into the database table.
   *
   * @param ship the ship data to be saved
   * @return {@code true} if the ship data did not previously exist and was successfully saved,
   * {@code false} otherwise
   */
  @PostMapping(value = "/saveShipData")
  public ResponseEntity<Boolean> saveShipData(@RequestBody Ship ship) {
    System.out.println("Empfangen: " + ship.toString());

    return shipService.save(ship);
  }

  @PutMapping(value = "/updateShipData")
  public ResponseEntity<Boolean> updateShipData(@RequestBody Ship ship) {
    System.out.println("Empfangen: " + ship.toString());

    return shipService.update(ship);
  }

  @GetMapping(value = "/getShipData")
  public ResponseEntity<Ship> getShipData(@RequestParam("shipId") String shipId) {
    System.out.println("Empfangen: " + shipId.toString());

    return shipService.getShipData(shipId);
  }

  @GetMapping(value = "/getAllShipData")
  public ResponseEntity<List<Ship>> getAllShipData() {

    return ResponseEntity.ok(shipService.getAllShipData());
  }

  /**
   * @return {@code true} if sector available (not in use by other ship), otherwise {@code false}
   */
  @GetMapping(value = "/findIfSectorInUse")
  public ResponseEntity<Boolean> isSectorAvailable(@RequestParam("sectorX") int sectorX, @RequestParam("sectorY") int sectorY) {
    System.out.println("Empfangen: " + "(" + sectorX + "," + sectorY + ")");

    return shipService.istSectorExist(sectorX, sectorY);
  }

  /**
   * @param shipId the ship data will be deleted from database.
   * @return {@code true} if the ship data exists and successfully deleted, otherwise {@code false}
   */
  @DeleteMapping(value = "/deleteShipData") //TODO check, if works correctly
  public ResponseEntity<Boolean> deleteShipData(@RequestParam("shipId") String shipId) {
    System.out.println("Empfangen: " + shipId);

    return shipService.delete(shipId);
  }

  @PostMapping(value = "/saveShipSector")
  public ResponseEntity<Boolean> saveShipSector(@RequestBody ShipSector shipSector){
    shipRouteService.save(shipSector);

    return ResponseEntity.ok(true);
  }

  @GetMapping(value = "/getShipRoute")
  public ResponseEntity<Map<String, List<ShipSector>>> getShipRoute(){
    Map<String, List<ShipSector>> shipSector = shipRouteService.findShipRoute();

    return ResponseEntity.ok(shipSector);
  }

  @GetMapping(value = "/checkShipIdExists")
  public ResponseEntity<Boolean> isShipIdExist(@RequestParam("shipId") String shipId) {

    return shipService.existShipId(shipId);
  }

  @PostMapping(value = "/saveRoutePlan")
  public ResponseEntity<Boolean> saveRoutePlan(@RequestBody RoutePlan routePlan) {
    System.out.println("Empfangen: " + routePlan.toString());

    return routePlanService.save(routePlan);
  }

  @PostMapping(value = "/deleteAllRoutePlan")
  public ResponseEntity<Boolean> deleteAllRoutePlan() {
    System.out.println("The deletion of all route plans has been requested.");

    return routePlanService.deleteAllRoutePlan();
  }

}
