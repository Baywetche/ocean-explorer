package com.hhs.shipbaseserver.dao;

import com.hhs.lib.model.ShipData;
import com.hhs.lib.model.Vec2D;
import com.hhs.shipbaseserver.repository.ShipDataRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

  private boolean isSectorUsedAlready(int sectorX, int sectorY) {
    return !shipDataRepository.findBySectorXAndSectorY(sectorX, sectorY).isEmpty();
  }

  public ResponseEntity<Boolean> delete(ShipData shipData) {
    if (shipDataRepository.findByShipId(shipData.getShipId()).isEmpty())
      return ResponseEntity.ok(false);

    shipDataRepository.delete(shipData);
    return ResponseEntity.ok(true);
  }

  /**
   * sector liegt in datenbank nicht vor -> sector ist frei: true
   *
   * sector liegt in datenbank vor -> sector ist belegt: false
   * */
  public ResponseEntity<Boolean> findBySector(Vec2D sector) {
    boolean sectorFree = shipDataRepository.findBySectorXAndSectorY(sector.getX(), sector.getY()).isEmpty();

    return ResponseEntity.ok(sectorFree);
  }

}
