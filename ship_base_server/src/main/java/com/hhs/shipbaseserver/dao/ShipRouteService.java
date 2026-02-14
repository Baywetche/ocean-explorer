package com.hhs.shipbaseserver.dao;

import com.hhs.lib.model.ShipSector;
import com.hhs.shipbaseserver.repository.ShipRouteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShipRouteService {

  private final ShipRouteRepository shipRouteRepository;

  public ShipRouteService(ShipRouteRepository shipRouteRepository) {this.shipRouteRepository = shipRouteRepository;}

  public boolean save(ShipSector shipSector) {
    if (isSectorExists(shipSector.getShipSectorX(), shipSector.getShipSectorY())) {
      return false;
    }

    shipRouteRepository.save(shipSector);

    return true;
  }

  private boolean isSectorExists(int sectorX, int sectorY) {
    return !shipRouteRepository.findByShipSectorXAndShipSectorY(sectorX, sectorY).isEmpty();
  }

  public boolean deleteAllShipRouteFromDB() {

    shipRouteRepository.deleteAll();

    return true;
  }

  public boolean deleteShipSectorBySectorXAndSectorY(ShipSector shipSector) {
    if (isSectorExists(shipSector.getShipSectorX(), shipSector.getShipSectorY())) {
      return false;
    }

    shipRouteRepository.deleteByShipSectorXAndShipSectorY(shipSector.getShipSectorX(), shipSector.getShipSectorY());

    return true;
  }

  public Map<String, List<ShipSector>> findShipRoute() {

    Map<String, List<ShipSector>> shipSectorMap = new HashMap<>();

    for (ShipSector shipSector : shipRouteRepository.findAll()) {

      String shipId = shipSector.getShipId();

      List<ShipSector> list = shipSectorMap.get(shipId);

      if (list == null) {
        list = new ArrayList<>();
        shipSectorMap.put(shipId, list);
      }

      list.add(shipSector);
    }

    return shipSectorMap;
  }

}
