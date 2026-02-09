package com.hhs.shipbaseserver.repository;

import com.hhs.lib.model.Ship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipRepository extends JpaRepository<Ship, Long> {

  Optional<Ship> findByShipId(String shipId);

  List<Ship> findBySectorXAndSectorY(int x, int y);

  List<Ship> findByShipIdAndDirectionX(String shipId, int directionX);

  boolean existsBySectorXAndSectorY(int sectorX, int sectorY);

  boolean existsByShipId(String shipId);

  void deleteByShipId(String shipId);

}
