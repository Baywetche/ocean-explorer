package com.hhs.lib.model;

public enum ShipGoalDirection {
  NORTH(new Vec2D(0, 1)),
  SOUTH(new Vec2D(0, -1)),
  WEST(new Vec2D(-1, 0));

  private Vec2D key;

  public Vec2D getKey() {
    return key;
  }

  ShipGoalDirection(Vec2D key) {
    this.key = key;
  }

}
