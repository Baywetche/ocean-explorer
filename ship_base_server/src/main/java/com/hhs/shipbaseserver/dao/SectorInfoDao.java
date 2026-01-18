package com.hhs.ship_base_server.dao;

import com.hhs.ship_base_server.model.SectorInfo;
import com.hhs.ship_base_server.repository.SectorInfoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SectorInfoDao {

  private SectorInfoRepository sectorInfoRepository;

  public SectorInfoDao(SectorInfoRepository sectorInfoRepository) {
    this.sectorInfoRepository = sectorInfoRepository;
  }

  public SectorInfo save(SectorInfo sectorInfo) {
    return sectorInfoRepository.save(sectorInfo);

  }

  private List<SectorInfo> getAllSectorInfos() {
    return sectorInfoRepository.findAll();
  }

  public SectorInfo getSectorInfoById(String id) throws ClassNotFoundException {
    Optional<SectorInfo> sectorInfo = sectorInfoRepository.findById(Long.valueOf(id));
    if (sectorInfo.isEmpty()) {
      throw new ClassNotFoundException("sector info not exist");
    }
    return sectorInfo.get();
  }

}
