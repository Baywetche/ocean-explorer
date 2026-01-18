package com.hhs.shipapp.models;

import com.hhs.lib.model.Sector;
import com.hhs.lib.model.Ground;
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
