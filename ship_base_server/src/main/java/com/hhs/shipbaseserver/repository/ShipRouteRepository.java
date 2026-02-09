package com.hhs.shipbaseserver.repository;

import com.hhs.lib.model.ShipSector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipRouteRepository extends JpaRepository<ShipSector, Long> {

  List<ShipSector> findByShipSectorXAndShipSectorY(int shipSectorX, int shipSectorY);

  void deleteByShipSectorXAndShipSectorY(int x, int y);

}
