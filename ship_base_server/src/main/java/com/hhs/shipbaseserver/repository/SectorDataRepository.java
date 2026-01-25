package com.hhs.shipbaseserver.repository;

import com.hhs.lib.model.SectorData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectorDataRepository extends JpaRepository<SectorData, Long> {

  List<SectorData> findByShipId(String shipId);

  List<SectorData> findBySectorXAndSectorY(int x, int y);
}
