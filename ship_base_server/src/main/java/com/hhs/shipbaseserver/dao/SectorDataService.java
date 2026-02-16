package com.hhs.shipbaseserver.dao;

import com.hhs.lib.model.SectorData;
import com.hhs.shipbaseserver.repository.SectorDataRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SectorDataService {

  private SectorDataRepository sectorDataRepository;

  public SectorDataService(SectorDataRepository sectorDataRepository) {
    this.sectorDataRepository = sectorDataRepository;
  }

  /**
   * Saves the given SectorData in the database if no complete entry exists yet.
   *
   * @param sectorData the sector data to be saved
   * @return {@code true} if the entity was saved or updated, {@code false} if an entry already existed
   */
  public boolean save(SectorData sectorData) {

    List<SectorData> existingSectors = sectorDataRepository.findBySectorXAndSectorY(sectorData.getSectorX(), sectorData.getSectorY());

    // Kein Eintrag vorhanden → neu speichern
    if (existingSectors.isEmpty()) {
      sectorDataRepository.save(sectorData);
      return true;
    }

    Long existingId = existingSectors.getFirst().getId();
    Optional<SectorData> existingOptional = sectorDataRepository.findById(existingId);
    if (existingOptional.isEmpty()) {
      // wenn Datensatz wurde zwischenzeitlich gelöscht
      sectorDataRepository.save(sectorData);
      return true;
    }

    SectorData existing = existingOptional.get();
    // Eintrag existiert, aber Ground ist noch nicht gesetzt → ergänzen
    if (existing.getGround() == null) {
      existing.setGround(sectorData.getGround());
      existing.setHeight(sectorData.getHeight());

      sectorDataRepository.save(existing);
      return true;
    }

    // Eintrag existiert und ist bereits vollständig
    return false;
  }

  /**
   * @return all existing sector data as a list*/
  public List<SectorData> getAllSectorData() {
    return sectorDataRepository.findAll();
  }

  /**
   * Searches for a certain sector.
   *
   * @return {@code true} if sector found, {@code false} if sector not found
   * */
  public SectorData findBySector(int sectorX, int sectorY) {
    boolean exists = !sectorDataRepository.findBySectorXAndSectorY(sectorX, sectorY).isEmpty();

    SectorData sectorData = sectorDataRepository.findBySectorXAndSectorY(sectorX, sectorY).getFirst();
    if (exists) {
      return sectorData;
    }

    return new SectorData();
  }

  /**
   * Updates sector data in DB
   *
   * @return {@code true} if sector found and updated successfully, {@code false} if sector not found*/
  public boolean update(SectorData sectorData) {
    try {
      Long id = findId4GivenSectorData(sectorData);
      Optional<SectorData> sectorDataOptional = sectorDataRepository.findById(id);

      if (sectorDataOptional.isEmpty()) {
        return false;
      }

      SectorData existing = sectorDataOptional.get();
      existing.setDepth(sectorData.getDepth());
      existing.setStddev(sectorData.getStddev());

      sectorDataRepository.save(existing);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  /**
   * Searches for an id for given sector data.
   *
   * @return id, if sector exist, otherwise throws exception*/
  private Long findId4GivenSectorData(SectorData sectorData) throws ClassNotFoundException {
    List<SectorData> sectorDataList = sectorDataRepository.findBySectorXAndSectorY(sectorData.getSectorX(), sectorData.getSectorY());
    if (sectorDataList.isEmpty()) {
      throw new ClassNotFoundException("sector data not exist");
    }

    return sectorDataList.getFirst().getId();
  }

}
