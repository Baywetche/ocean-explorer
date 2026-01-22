package com.hhs.shipbaseserver.dao;

import com.hhs.lib.model.ShipData;
import com.hhs.shipbaseserver.repository.ShipDataRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShipDataDao {

  private final ShipDataRepository shipDataRepository;

  public ShipDataDao(ShipDataRepository shipDataRepository) {
    this.shipDataRepository = shipDataRepository;
  }

  public ResponseEntity<Boolean> save(ShipData shipData) {
    if (isSectorUsedAlready(shipData.getSectorX(), shipData.getSectorY())) {
      return ResponseEntity.ok(false);
    }

    shipDataRepository.save(shipData);

    return ResponseEntity.ok(true);
  }

  public ResponseEntity<Boolean> update(String shipId, ShipData shipData) {
    Optional<ShipData> optionalShipData = shipDataRepository.findByShipId(shipId);

    if (optionalShipData.isEmpty()) {
      return ResponseEntity.ok(false);
    }

    ShipData newShipData = optionalShipData.get();
    newShipData.setShipId(shipId);
    newShipData.setShipName(shipData.getShipName());
    newShipData.setSectorX(shipData.getSectorX());
    newShipData.setSectorY(shipData.getSectorY());
    newShipData.setDirectionX(shipData.getDirectionX());
    newShipData.setDirectionY(shipData.getDirectionY());

    save(newShipData);
    return ResponseEntity.ok(true);
  }

  private boolean isSectorUsedAlready(int sectorX, int sectorY) {
    return !shipDataRepository.findBySectorXAndSectorY(sectorX, sectorY).isEmpty();
  }

  public ResponseEntity<Boolean> delete(ShipData shipData) {
    if (shipDataRepository.findByShipId(shipData.getShipId()).isEmpty())
      return ResponseEntity.ok(false);

    shipDataRepository.delete(shipData);
    return ResponseEntity.ok(true);
  }


/*  public ResponseEntity<Boolean> findBySector(int sectorX, int sectorY) {
    boolean sectorFree = shipDataRepository.findBySectorXAndSectorY(sectorX, sectorY).isEmpty();

    return ResponseEntity.ok(sectorFree);
  }*/

  /**
   * sector liegt in datenbank vor -> sector ist belegt: false
   *
   * sector liegt in datenbank nicht vor -> sector ist frei: true
   * */
  public ResponseEntity<Boolean> istSectorExist(int sectorX, int sectorY) {
    return ResponseEntity.ok(!shipDataRepository.existsBySectorXAndSectorY(sectorX, sectorY));
  }

}
