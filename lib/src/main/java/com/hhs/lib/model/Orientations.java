package com.hhs.lib.model;

import java.util.ArrayList;
import java.util.List;

public class Orientations {
  private List<Vec2D> orientationsList = new ArrayList<>(List.of(
      new Vec2D(0, 1),
      new Vec2D(1, 1),
      new Vec2D(1, 0),
      new Vec2D(1, -1),
      new Vec2D(0, -1),
      new Vec2D(-1, -1),
      new Vec2D(-1, 0),
      new Vec2D(-1, 1)
  ));

  public List<Vec2D> getOrientationsList() {
    return orientationsList;
  }
}
