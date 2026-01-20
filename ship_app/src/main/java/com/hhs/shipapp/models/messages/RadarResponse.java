package com.hhs.shipapp.models.messages;

import com.hhs.lib.model.Sector;
import com.hhs.lib.model.Vec2D;
import com.hhs.shipapp.models.Echo;
import com.hhs.shipapp.models.enums.Commands;
import lombok.Data;

import java.util.List;

@Data
public class RadarResponse {
  private List<Echo> echos;
  private List<Sector> notNavigable;
}