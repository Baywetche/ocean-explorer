package com.hhs.shipapp.models;

import lombok.Data;

import java.util.List;

@Data
public class RadarResponse {
  private List<Echo> echos;
  private List<Sector> notNavigable;
}