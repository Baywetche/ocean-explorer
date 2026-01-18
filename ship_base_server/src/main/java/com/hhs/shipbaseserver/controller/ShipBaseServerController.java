package com.hhs.shipbaseserver.controller;

import com.hhs.lib.model.ShipData;
import com.hhs.shipbaseserver.dao.SectorInfoDao;
import com.hhs.shipbaseserver.dao.ShipDataDao;
import com.hhs.shipbaseserver.model.SectorInfo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipBaseServerAPI")
public class DataCollector {
  private final SectorInfoDao sectorInfoDao;
  private final ShipDataDao shipDataDao;

  public DataCollector(SectorInfoDao sectorInfoDao, ShipDataDao shipDataDao) {
    this.sectorInfoDao = sectorInfoDao;
    this.shipDataDao = shipDataDao;
  }

  @PostMapping(value = "/saveSectorInfo")
  public boolean saveSectorInfo(@RequestBody SectorInfo message) {
    System.out.println("Empfangen: " + message.toString());

    return sectorInfoDao.save(message);
  }

  @PostMapping(value = "/saveShipDataDao")
  public boolean saveShipData(@RequestBody ShipData shipData){
    System.out.println("Empfangen: " + shipData.toString());

    return shipDataDao.save(shipData);
  }

  @DeleteMapping(value = "/deleteShipData")
  public boolean deleteShipData(@RequestBody ShipData shipData){
    System.out.println("Empfangen: " + shipData.toString());

    return shipDataDao.delete(shipData);
  }

}
