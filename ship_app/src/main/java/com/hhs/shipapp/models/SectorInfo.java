package com.hhs.shipapp.models;

import com.hhs.lib.model.Ground;
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
