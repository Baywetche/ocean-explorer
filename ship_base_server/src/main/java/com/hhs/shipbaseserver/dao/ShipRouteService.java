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

  public ResponseEntity<Boolean> save(ShipSector shipSector) {
    if (isSectorExists(shipSector.getShipSectorX(), shipSector.getShipSectorY())) {
      return ResponseEntity.ok(false);
    }

    shipRouteRepository.save(shipSector);

    return ResponseEntity.ok(true);

  }

  private boolean isSectorExists(int sectorX, int sectorY) {
    return !shipRouteRepository.findByShipSectorXAndShipSectorY(sectorX, sectorY).isEmpty();
  }

  public ResponseEntity<Boolean> deleteAllShipRouteFromDB() {

    shipRouteRepository.deleteAll();

    return ResponseEntity.ok(true);
  }

  public ResponseEntity<Boolean> deleteShipSectorBySectorXAndSectorY(ShipSector shipSector) {
    if (isSectorExists(shipSector.getShipSectorX(), shipSector.getShipSectorY())) {
      return ResponseEntity.ok(false);
    }

    shipRouteRepository.deleteByShipSectorXAndShipSectorY(shipSector.getShipSectorX(), shipSector.getShipSectorY());

    return ResponseEntity.ok(true);
  }

 /* public Map<String, List<ShipSector>> findShipRoute() {
    Map<String, List<ShipSector>> shipSectorMap = new HashMap<>();
    Set<String> shipIdSet = new HashSet<>();

    List<ShipSector> allShipSectorList = new ArrayList<>(shipRouteRepository.findAll());

    for (ShipSector shipSector : allShipSectorList){
      shipIdSet.add(shipSector.getShipId());
    }

    for (String shipId : shipIdSet){
      List<ShipSector> shipSectorList4ShipId = new ArrayList<>();
      for (ShipSector shipSector : allShipSectorList){
        if (shipSector.getShipId().equals(shipId)){
          shipSectorList4ShipId.add(shipSector);
        }
      }

      shipSectorMap.put(shipId, shipSectorList4ShipId);
    }

    return shipSectorMap;
  }*/

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
