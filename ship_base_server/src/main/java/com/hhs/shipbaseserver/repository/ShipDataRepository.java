package com.hhs.shipbaseserver.repository;

import com.hhs.lib.model.ShipData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipDataRepository extends JpaRepository<ShipData, Long> {

  Optional<ShipData> findByShipId(String shipId);

  List<ShipData> findBySectorXAndSectorY(int x, int y);

  List<ShipData> findByShipIdAndDirectionX(String shipId, int directionX);

  boolean existsBySectorXAndSectorY(int sectorX, int sectorY);

  boolean existsByShipId(String shipId);

  void deleteByShipId(String shipId);

}
