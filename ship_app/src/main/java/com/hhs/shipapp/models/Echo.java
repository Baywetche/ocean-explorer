package com.hhs.ocean_explorer.models;

import com.hhs.ocean_explorer.models.enums.Ground;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Echo {

  private Sector sector;
  private int height;
  private Ground ground;

}
