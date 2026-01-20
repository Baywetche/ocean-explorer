package com.hhs.lib.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class SectorInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  private String shipId;
  private Ground ground;
  private int depth;
  private int sectorX;
  private int sectorY;

}
