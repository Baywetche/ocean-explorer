package com.hhs.shipbaseserver.dao;

import com.hhs.lib.model.ShipSector;
import com.hhs.shipbaseserver.repository.ShipRouteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShipRouteService {

  private final ShipRouteRepository shipRouteRepository;

  public ShipRouteService(ShipRouteRepository shipRouteRepository) {this.shipRouteRepository = shipRouteRepository;}

  public ResponseEntity<Boolean> save(ShipSector shipSector) {
    if (isSectorExists(shipSector.getShipSectorX(), shipSector.getShipSectorY())) {
      return ResponseEntity.ok(false);
    }

    shipRouteRepository.save(shipSector);

    return ResponseEntity.ok(true);

  }

  private boolean isSectorExists(int sectorX, int sectorY) {
    return !shipRouteRepository.findBySectorXAndSectorY(sectorX, sectorY).isEmpty();
  }

  public ResponseEntity<Boolean> deleteAllShipRouteFromDB() {

    shipRouteRepository.deleteAll();

    return ResponseEntity.ok(true);
  }

  public ResponseEntity<Boolean> deleteShipRouteBySectorXAndSectorY(ShipSector shipSector){
    if (isSectorExists(shipSector.getShipSectorX(), shipSector.getShipSectorY())){
      return ResponseEntity.ok(false);
    }

    shipRouteRepository.deleteByShipSectorXAndShipSectorY(shipSector.getShipSectorX(), shipSector.getShipSectorY());

    return ResponseEntity.ok(true);
  }

  public List<ShipSector> findShipRoute() {
    return shipRouteRepository.findAll();
  }
}
