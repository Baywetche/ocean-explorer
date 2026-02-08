package com.hhs.shipapp.models;

import com.hhs.lib.model.Sector;
import lombok.Data;

import java.util.List;

@Data
public class RadarResponse {
  private List<Echo> echos;
  private List<Sector> notNavigable;
}