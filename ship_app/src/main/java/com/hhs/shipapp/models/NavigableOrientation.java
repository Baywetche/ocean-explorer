package com.hhs.shipapp.models;

import lombok.Data;

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

