package com.hhs.lib.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class ShipSector {

  @Id
  @GeneratedValue()
  private Long id;

  private String shipId;
  private int shipSectorX;
  private int shipSectorY;

}
