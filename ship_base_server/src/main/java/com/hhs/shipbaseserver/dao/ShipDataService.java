package com.hhs.shipbaseserver.dao;

import com.hhs.lib.model.ShipData;
import com.hhs.shipbaseserver.repository.ShipDataRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ShipDataService {

  private final ShipDataRepository shipDataRepository;

  public ShipDataService(ShipDataRepository shipDataRepository) {
    this.shipDataRepository = shipDataRepository;
  }

  public ResponseEntity<Boolean> save(ShipData shipData) {
    if (isSectorUsedAlready(shipData.getSectorX(), shipData.getSectorY())) {
      return ResponseEntity.ok(false);
    }

    shipDataRepository.save(shipData);

    return ResponseEntity.ok(true);
  }

  public ResponseEntity<Boolean> update(ShipData shipData) {
    Optional<ShipData> optionalShipData = shipDataRepository.findByShipId(shipData.getShipId());

    if (optionalShipData.isEmpty()) {
      return ResponseEntity.ok(false);
    }

    ShipData newShipData = optionalShipData.get();
    newShipData.setSectorX(shipData.getSectorX());
    newShipData.setSectorY(shipData.getSectorY());
    newShipData.setDirectionX(shipData.getDirectionX());
    newShipData.setDirectionY(shipData.getDirectionY());

    shipDataRepository.save(newShipData);

    return ResponseEntity.ok(true);
  }

  private boolean isSectorUsedAlready(int sectorX, int sectorY) {
    return !shipDataRepository.findBySectorXAndSectorY(sectorX, sectorY).isEmpty();
  }

  @Transactional
  public ResponseEntity<Boolean> delete(String shipId) {
    if (shipDataRepository.findByShipId(shipId).isEmpty()) {
      return ResponseEntity.ok(false);
    }

    shipDataRepository.deleteByShipId(shipId);

    return ResponseEntity.ok(true);
  }

  /**
   * sector liegt in datenbank vor -> sector ist belegt: false
   * <p>
   * sector liegt in datenbank nicht vor -> sector ist frei: true
   */
  public ResponseEntity<Boolean> istSectorExist(int sectorX, int sectorY) {
    return ResponseEntity.ok(!shipDataRepository.existsBySectorXAndSectorY(sectorX, sectorY));
  }

  public ResponseEntity<ShipData> getShipData(String shipId) {
    Optional<ShipData> optionalShipData = shipDataRepository.findByShipId(shipId);
    if (optionalShipData.isEmpty()) {
      return ResponseEntity.ok(null);
    }

    return ResponseEntity.ok(optionalShipData.get());
  }

  public ResponseEntity<Boolean> existShipId(String shipId) {
    if (!shipDataRepository.existsByShipId(shipId))
      return ResponseEntity.ok(false);

    System.out.println();
    return ResponseEntity.ok(true);
  }
}
