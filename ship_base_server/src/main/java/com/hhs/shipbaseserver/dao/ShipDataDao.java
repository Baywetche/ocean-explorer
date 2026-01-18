package com.hhs.shipbaseserver.dao;

import com.hhs.lib.model.ShipData;
import com.hhs.lib.model.Vec2D;
import com.hhs.shipbaseserver.repository.ShipDataRepository;
import org.springframework.stereotype.Service;

@Service
public class ShipDataDao {
    private final ShipDataRepository shipDataRepository;

    public ShipDataDao(ShipDataRepository shipDataRepository) {
        this.shipDataRepository = shipDataRepository;
    }

    public boolean save(ShipData shipData) {
        if (isSectorUsedAlready(shipData.getSectorX(), shipData.getSectorY())) return false;

        shipDataRepository.save(shipData);

        return true;
    }

    private boolean isSectorUsedAlready(int sectorX, int sectorY) {
        return !shipDataRepository.findBySectorXAndSectorY(sectorX, sectorY).isEmpty();
    }

    public boolean delete(ShipData shipData) {
        if (shipDataRepository.findByShipId(shipData.getShipId()).isEmpty()) return false;

        shipDataRepository.delete(shipData);
        return true;
    }

    public boolean findBySector(Vec2D sector) {
        return shipDataRepository.findBySectorXAndSectorY(sector.getX(), sector.getY()).isEmpty();
    }


}
