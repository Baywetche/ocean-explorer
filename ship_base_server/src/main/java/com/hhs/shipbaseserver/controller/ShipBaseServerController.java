package com.hhs.shipbaseserver.controller;

import com.hhs.lib.model.ShipData;
import com.hhs.lib.model.Vec2D;
import com.hhs.shipbaseserver.dao.SectorInfoDao;
import com.hhs.shipbaseserver.dao.ShipDataDao;
import com.hhs.shipbaseserver.model.SectorInfo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipBaseServerAPI")
public class ShipBaseServerController {
    private final SectorInfoDao sectorInfoDao;
    private final ShipDataDao shipDataDao;

    public ShipBaseServerController(SectorInfoDao sectorInfoDao, ShipDataDao shipDataDao) {
        this.sectorInfoDao = sectorInfoDao;
        this.shipDataDao = shipDataDao;
    }

    /**
     * Saves the given sector information into the database.
     *
     * @param sectorInfo the sector information to be saved
     * @return {@code true} if the sector information was successfully saved, {@code false} otherwise
     */
    @PostMapping(value = "/saveSectorInfo")
    public boolean saveSectorInfo(@RequestBody SectorInfo sectorInfo) {
        System.out.println("Empfangen: " + sectorInfo.toString());

        return sectorInfoDao.save(sectorInfo);
    }

    /**
     * Checks whether the specified sector exists in the database table.
     *
     * @return {@code true} if the sector exists, otherwise {@code false}
     */
    @PostMapping(value = "/findSectorInfo")
    public boolean findSectorInfo(@RequestBody SectorInfo sectorInfo) {
        System.out.println("Empfangen: " + sectorInfo.toString());

        return sectorInfoDao.findBySector(sectorInfo);
    }

    /**
     * Saves the given ship data into the database table.
     *
     * @param shipData the ship data to be saved
     * @return {@code true} if the ship data did not previously exist and was successfully saved,
     * {@code false} otherwise
     */
    @PostMapping(value = "/saveShipDataDao")
    public boolean saveShipData(@RequestBody ShipData shipData) {
        System.out.println("Empfangen: " + shipData.toString());

        return shipDataDao.save(shipData);
    }

    /**
     * @param shipData the ship data will be deleted from database.
     * @return {@code true} if the ship data exists and successfully deleted, otherwise {@code false}
     */
    @DeleteMapping(value = "/deleteShipData")
    public boolean deleteShipData(@RequestBody ShipData shipData) {
        System.out.println("Empfangen: " + shipData.toString());

        return shipDataDao.delete(shipData);
    }

    @DeleteMapping(value = "/findIfSectorInUse")
    public boolean isSectorInUse(@RequestBody Vec2D sector) {
        System.out.println("Empfangen: " + sector);

        return shipDataDao.findBySector(sector);
    }

}
