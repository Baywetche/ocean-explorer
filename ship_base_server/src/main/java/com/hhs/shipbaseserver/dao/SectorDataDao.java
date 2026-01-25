package com.hhs.shipbaseserver.dao;

import com.hhs.lib.model.SectorData;
import com.hhs.shipbaseserver.repository.SectorDataRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SectorDataDao {

  private SectorDataRepository sectorDataRepository;

  public SectorDataDao(SectorDataRepository sectorDataRepository) {
    this.sectorDataRepository = sectorDataRepository;
  }

  public ResponseEntity<Boolean> save(SectorData sectorData) {
    boolean exists = !sectorDataRepository.findBySectorXAndSectorY(sectorData.getSectorX(), sectorData.getSectorY())
                                          .isEmpty();

    if (exists) {
      return ResponseEntity.ok(false);
    }

    sectorDataRepository.save(sectorData);
    return ResponseEntity.ok(true);
  }

  public List<SectorData> getAllSectorData() {
    return sectorDataRepository.findAll();
  }

  public ResponseEntity<Boolean> findBySector(SectorData sectorData) {
    boolean exists = !sectorDataRepository.findBySectorXAndSectorY(sectorData.getSectorX(), sectorData.getSectorY())
                                          .isEmpty();

    if (exists) {
      return ResponseEntity.ok(false);
    }

    return ResponseEntity.ok(true);
  }


  /**
   * return all sector data, that explored by one ship
   */
  public List<SectorData> getAllSectorDataByShipId(String shipId) throws ClassNotFoundException {
    List<SectorData> sectorData = sectorDataRepository.findByShipId(shipId);

    if (sectorData.isEmpty()) {
      throw new ClassNotFoundException("sector data not exist");
    }

    return sectorData;
  }

  public ResponseEntity<Boolean> update(SectorData sectorData) {
    try {
      Long id = getSectorDataIdByShipId(sectorData);
      Optional<SectorData> sectorDataOptional = sectorDataRepository.findById(id);

      if (sectorDataOptional.isEmpty()) {
        return ResponseEntity.ok(false);
      }

      SectorData existing = sectorDataOptional.get();
      existing.setSectorX(sectorData.getSectorX());
      existing.setSectorY(sectorData.getSectorY());
      existing.setHeight(sectorData.getHeight());
      existing.setDepth(sectorData.getDepth());
      existing.setStddev(sectorData.getStddev());

      sectorDataRepository.save(existing);
      return ResponseEntity.ok(true);
    } catch (ClassNotFoundException e) {
      return ResponseEntity.ok(false);
    }
  }

  private Long getSectorDataIdByShipId(SectorData sectorData) throws ClassNotFoundException {
    List<SectorData> sectorDataList = sectorDataRepository.findBySectorXAndSectorY(sectorData.getSectorX(),
                                                                                   sectorData.getSectorY());
    if (sectorDataList.isEmpty()) {
      throw new ClassNotFoundException("sector data not exist");
    }

    return sectorDataList.getFirst().getId();
  }

}
