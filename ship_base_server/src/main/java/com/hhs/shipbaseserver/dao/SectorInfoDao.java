package com.hhs.shipbaseserver.dao;

import com.hhs.shipbaseserver.model.SectorInfo;
import com.hhs.shipbaseserver.repository.SectorInfoRepository;
import org.apache.coyote.Response;
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

  public ResponseEntity<Boolean> save(SectorInfo sectorInfo) {

    boolean exists = !sectorInfoRepository.findBySectorXAndSectorY(sectorInfo.getSectorX(), sectorInfo.getSectorY()).isEmpty();

    if (exists) {
      return ResponseEntity.ok(false);
    }

    sectorInfoRepository.save(sectorInfo);
    return ResponseEntity.ok(true);
  }

  public List<SectorInfo> getAllSectorInfos() {
    return sectorInfoRepository.findAll();
  }

  public ResponseEntity<Boolean> findBySector(SectorInfo sectorInfo) {
    boolean exists = !sectorInfoRepository.findBySectorXAndSectorY(sectorInfo.getSectorX(), sectorInfo.getSectorY()).isEmpty();

    if (exists){
      return ResponseEntity.ok(false);
    }

    return ResponseEntity.ok(true);
  }

  public SectorInfo getSectorInfoById(String id) throws ClassNotFoundException {
    Optional<SectorInfo> sectorInfo = sectorInfoRepository.findById(Long.valueOf(id));
    if (sectorInfo.isEmpty()) {
      throw new ClassNotFoundException("sector info not exist");
    }
    return sectorInfo.get();
  }

}
