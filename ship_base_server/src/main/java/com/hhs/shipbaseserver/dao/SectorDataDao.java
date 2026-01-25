package com.hhs.shipbaseserver.dao;

import com.hhs.lib.model.SectorData;
import com.hhs.shipbaseserver.repository.SectorInfoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SectorInfoDao {

  private SectorInfoRepository sectorInfoRepository;

  public SectorInfoDao(SectorInfoRepository sectorInfoRepository) {
    this.sectorInfoRepository = sectorInfoRepository;
  }

  public ResponseEntity<Boolean> save(SectorData sectorData) {

    boolean exists = !sectorInfoRepository.findBySectorXAndSectorY(sectorData.getSectorX(), sectorData.getSectorY()).isEmpty();

    if (exists) {
      return ResponseEntity.ok(false);
    }

    sectorInfoRepository.save(sectorData);
    return ResponseEntity.ok(true);
  }

  public List<SectorData> getAllSectorInfos() {
    return sectorInfoRepository.findAll();
  }

  public ResponseEntity<Boolean> findBySector(SectorData sectorData) {
    boolean exists = !sectorInfoRepository.findBySectorXAndSectorY(sectorData.getSectorX(), sectorData.getSectorY()).isEmpty();

    if (exists) {
      return ResponseEntity.ok(false);
    }

    return ResponseEntity.ok(true);
  }

  public SectorData getSectorInfoById(String id) throws ClassNotFoundException {
    Optional<SectorData> sectorInfo = sectorInfoRepository.findById(Long.valueOf(id));
    if (sectorInfo.isEmpty()) {
      throw new ClassNotFoundException("sector info not exist");
    }
    return sectorInfo.get();
  }

}
