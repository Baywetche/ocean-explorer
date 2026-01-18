package com.hhs.shipapp.models.messages;

import com.hhs.shipapp.models.Echo;
import com.hhs.shipapp.models.enums.Commands;
import lombok.Data;

import java.util.List;

@Data
public class RadarResponse {
  private Commands cmd;
  private String id;
  private List<Echo> echos;
}