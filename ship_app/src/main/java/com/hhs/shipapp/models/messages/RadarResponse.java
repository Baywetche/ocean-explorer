package com.hhs.ocean_explorer.models.messages;

import com.hhs.ocean_explorer.models.Echo;
import com.hhs.ocean_explorer.models.enums.Commands;
import lombok.Data;

import java.util.List;

@Data
public class RadarResponse {
  private Commands cmd;
  private String id;
  private List<Echo> echos;
}