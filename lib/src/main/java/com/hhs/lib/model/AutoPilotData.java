package com.hhs.lib.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutoPilotData {

  private String shipId;
  private Vec2D shipPosition;
  private List<SectorData> sectorDataList;

}
