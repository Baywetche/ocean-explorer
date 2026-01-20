package com.hhs.shipapp.models;

import com.hhs.lib.model.*;
import com.hhs.shipapp.models.messages.RadarResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NavigableOrientation {

  private boolean n = true;
  private boolean ne = true;
  private boolean e = false;
  private boolean se = true;
  private boolean s = true;
  private boolean sw = true;
  private boolean w = false;
  private boolean nw = true;

  public NavigableOrientation() {}

}

