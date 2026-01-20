package com.hhs.shipbaseserver.repository;

import com.hhs.lib.model.SectorInfo;
import com.hhs.lib.model.ShipData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectorInfoRepository extends JpaRepository<SectorInfo, Long> {

  List<ShipData> findByShipId(String shipId);

  List<ShipData> findBySectorXAndSectorY(int x, int y);
}
