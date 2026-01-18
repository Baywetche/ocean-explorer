package com.hhs.shipbaseserver.dao;

import com.hhs.shipbaseserver.model.SectorInfo;
import com.hhs.shipbaseserver.repository.SectorInfoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SectorInfoDao {

  private SectorInfoRepository sectorInfoRepository;

  public SectorInfoDao(SectorInfoRepository sectorInfoRepository) {
    this.sectorInfoRepository = sectorInfoRepository;
  }

  public boolean save(SectorInfo sectorInfo) {
    if (!sectorInfoRepository.findBySectorXAndSectorY(sectorInfo.getSectorX(), sectorInfo.getSectorY()).isEmpty()) return false;

    sectorInfoRepository.save(sectorInfo);

    return true;
  }

  private List<SectorInfo> getAllSectorInfos() {
    return sectorInfoRepository.findAll();
  }

  public boolean findBySector(SectorInfo sectorInfo) {
    return !sectorInfoRepository.findBySectorXAndSectorY(sectorInfo.getSectorX(), sectorInfo.getSectorY()).isEmpty();
  }

  public SectorInfo getSectorInfoById(String id) throws ClassNotFoundException {
    Optional<SectorInfo> sectorInfo = sectorInfoRepository.findById(Long.valueOf(id));
    if (sectorInfo.isEmpty()) {
      throw new ClassNotFoundException("sector info not exist");
    }
    return sectorInfo.get();
  }

}
