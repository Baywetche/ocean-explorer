package com.hhs.shipbaseserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hhs.lib.model.ShipData;

import java.util.List;

public interface ShipDataRepository extends JpaRepository<ShipData, Long> {

    List<ShipData> findByShipId(String shipId);

    List<ShipData> findBySectorXAndSectorY(int x, int y);

    List<ShipData> findByShipIdAndDirectionX(String shipId, int dirX);


}
