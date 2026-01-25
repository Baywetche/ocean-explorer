package com.hhs.lib.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@ToString
public class SectorData {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Long id;

  @Enumerated(EnumType.STRING)
  private Ground ground;

  private String shipId;
  private int sectorX;
  private int sectorY;
  private int height;
  private int depth;
  private float stddev;

  public SectorData(String shipId, Ground ground, int depth, int sectorX, int sectorY) {
    this.shipId = shipId;
    this.ground = ground;
    this.depth = depth;
    this.sectorX = sectorX;
    this.sectorY = sectorY;
  }

  public SectorData(String shipId, int sectorX, int sectorY) {
    this.shipId = shipId;
    this.sectorX = sectorX;
    this.sectorY = sectorY;
  }

  public SectorData() {

  }
}
