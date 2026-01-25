package com.hhs.shipbaseserver.repository;

import com.hhs.lib.model.SectorData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectorDataRepository extends JpaRepository<SectorData, Long> {

  Optional<SectorData> findById(Long id);

  List<SectorData> findByShipId(String shipId);

  List<SectorData> findBySectorXAndSectorY(int x, int y);
}
