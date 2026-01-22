package com.hhs.lib.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class SectorData {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String shipId;

  @Enumerated(EnumType.STRING)
  private Ground ground;

  private int depth;
  private int sectorX;
  private int sectorY;

  public SectorData(String shipId, Ground ground, int depth, int sectorX, int sectorY) {
    this.shipId = shipId;
    this.ground = ground;
    this.depth = depth;
    this.sectorX = sectorX;
    this.sectorY = sectorY;
  }

  public SectorData() {

  }
}
