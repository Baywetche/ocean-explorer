package com.hhs.ocean_explorer.models;

import com.hhs.ocean_explorer.models.enums.Ground;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SectorInfo {
  private String shipId;
  private Ground ground;
  private int depth;
  private int x;
  private int y;

}
