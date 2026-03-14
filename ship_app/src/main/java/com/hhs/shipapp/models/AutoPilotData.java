package com.hhs.shipapp.models;

import com.hhs.lib.model.SectorData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutoPilotData {

  private String shipId;
  private ShipPosition shipPosition;
  private List<SectorData> sectorDataList;

}
