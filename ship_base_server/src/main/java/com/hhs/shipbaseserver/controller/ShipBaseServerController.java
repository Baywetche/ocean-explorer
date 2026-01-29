package com.hhs.shipbaseserver.controller;

import com.hhs.lib.model.SectorData;
import com.hhs.lib.model.ShipData;
import com.hhs.shipbaseserver.dao.SectorDataDao;
import com.hhs.shipbaseserver.dao.ShipDataDao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shipBaseServerAPI")
public class ShipBaseServerController {

  private final SectorDataDao sectorDataDao;
  private final ShipDataDao shipDataDao;

  public ShipBaseServerController(SectorDataDao sectorDataDao, ShipDataDao shipDataDao) {
    this.sectorDataDao = sectorDataDao;
    this.shipDataDao = shipDataDao;
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

    return sectorDataDao.save(sectorData);
  }

  @PutMapping(value = "/updateSectorData")
  public ResponseEntity<Boolean> updateSectorData(@RequestBody SectorData sectorData) {
//    System.out.println("Empfangen: " + sectorData.toString());

    return sectorDataDao.update(sectorData);
  }

  /**
   * Checks whether the specified sector exists in the database table.
   *
   * @return {@code true} if the sector exists, otherwise {@code false}
   */
  @PostMapping(value = "/findSectorData") //TODO check, if works correctly
  public ResponseEntity<Boolean> findSectorData(@RequestBody SectorData sectorData) {
//    System.out.println("Empfangen: " + sectorData.toString());

    return sectorDataDao.findBySector(sectorData);
  }

  /**
   * Retrieves all sector information entries from the database.
   *
   * @return a list of all {@link SectorData} objects
   */
  @GetMapping(value = "/allSectorData") //TODO check, if works correctly
  public List<SectorData> findAllSectorData() {
    return sectorDataDao.getAllSectorData();
  }

  /**
   * Saves the given ship data into the database table.
   *
   * @param shipData the ship data to be saved
   * @return {@code true} if the ship data did not previously exist and was successfully saved,
   * {@code false} otherwise
   */
  @PostMapping(value = "/saveShipData")
  public ResponseEntity<Boolean> saveShipData(@RequestBody ShipData shipData) {
    System.out.println("Empfangen: " + shipData.toString());

    return shipDataDao.save(shipData);
  }

  @PutMapping(value = "/updateShipData")
  public ResponseEntity<Boolean> updateShipData(@RequestBody ShipData shipData) {
    System.out.println("Empfangen: " + shipData.toString());

    return shipDataDao.update(shipData);
  }

  @GetMapping(value = "/getShipData")
  public ResponseEntity<ShipData> getShipData(@RequestParam("shipId") String shipId) {
    System.out.println("Empfangen: " + shipId.toString());

    return shipDataDao.getShipData(shipId);
  }

  /**
   * @return {@code true} if sector available (not in use by other ship), otherwise {@code false}
   */
  @GetMapping(value = "/findIfSectorInUse")
  public ResponseEntity<Boolean> isSectorAvailable(@RequestParam("sectorX") int sectorX, @RequestParam("sectorY") int sectorY) {
    System.out.println("Empfangen: " + "(" + sectorX + "," + sectorY + ")");

    return shipDataDao.istSectorExist(sectorX, sectorY);
  }

  /**
   * @param shipId the ship data will be deleted from database.
   * @return {@code true} if the ship data exists and successfully deleted, otherwise {@code false}
   */
  @DeleteMapping(value = "/deleteShipData") //TODO check, if works correctly
  public ResponseEntity<Boolean> deleteShipData(@RequestParam("shipId") String shipId) {
    System.out.println("Empfangen: " + shipId);

    return shipDataDao.delete(shipId);
  }

  @GetMapping(value = "/checkShipIdExists")
  public ResponseEntity<Boolean> isShipIdExist(@RequestParam("shipId") String shipId){

    return shipDataDao.existShipId(shipId);
  }

}
