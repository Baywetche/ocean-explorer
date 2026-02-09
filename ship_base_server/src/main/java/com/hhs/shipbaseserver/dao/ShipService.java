package com.hhs.shipbaseserver.dao;

import com.hhs.lib.model.Ship;
import com.hhs.shipbaseserver.repository.ShipRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ShipService {

  private final ShipRepository shipRepository;

  public ShipService(ShipRepository shipRepository) {
    this.shipRepository = shipRepository;
  }

  public ResponseEntity<Boolean> save(Ship ship) {
    if (isSectorUsedAlready(ship.getSectorX(), ship.getSectorY())) {
      return ResponseEntity.ok(false);
    }

    shipRepository.save(ship);

    return ResponseEntity.ok(true);
  }

  public ResponseEntity<Boolean> update(Ship ship) {
    Optional<Ship> optionalShipData = shipRepository.findByShipId(ship.getShipId());

    if (optionalShipData.isEmpty()) {
      return ResponseEntity.ok(false);
    }

    Ship newShip = optionalShipData.get();
    newShip.setSectorX(ship.getSectorX());
    newShip.setSectorY(ship.getSectorY());
    newShip.setDirectionX(ship.getDirectionX());
    newShip.setDirectionY(ship.getDirectionY());

    shipRepository.save(newShip);

    return ResponseEntity.ok(true);
  }

  private boolean isSectorUsedAlready(int sectorX, int sectorY) {
    return !shipRepository.findBySectorXAndSectorY(sectorX, sectorY).isEmpty();
  }

  @Transactional
  public ResponseEntity<Boolean> delete(String shipId) {
    if (shipRepository.findByShipId(shipId).isEmpty()) {
      return ResponseEntity.ok(false);
    }

    shipRepository.deleteByShipId(shipId);

    return ResponseEntity.ok(true);
  }

  /**
   * sector liegt in datenbank vor -> sector ist belegt: false
   * <p>
   * sector liegt in datenbank nicht vor -> sector ist frei: true
   */
  public ResponseEntity<Boolean> istSectorExist(int sectorX, int sectorY) {
    return ResponseEntity.ok(!shipRepository.existsBySectorXAndSectorY(sectorX, sectorY));
  }

  public ResponseEntity<Ship> getShipData(String shipId) {
    Optional<Ship> optionalShipData = shipRepository.findByShipId(shipId);
    if (optionalShipData.isEmpty()) {
      return ResponseEntity.ok(null);
    }

    return ResponseEntity.ok(optionalShipData.get());
  }

  public ResponseEntity<Boolean> existShipId(String shipId) {
    if (!shipRepository.existsByShipId(shipId))
      return ResponseEntity.ok(false);

    System.out.println();
    return ResponseEntity.ok(true);
  }

  public List<Ship> getAllShipData() {
    return shipRepository.findAll();
  }
}
