package com.hhs.lib.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class ShipSector {

  @Id
  private Long id;

  private int shipSectorX;
  private int shipSectorY;

}
