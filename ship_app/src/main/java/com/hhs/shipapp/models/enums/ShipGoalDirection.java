package com.hhs.shipapp.models.enums;

import com.hhs.shipapp.models.Vec2D;

public enum ShipGoalDirection {
  NORTH(new Vec2D(0, 1)),
  SOUTH(new Vec2D(0, -1)),
  SOUTH_WEST(new Vec2D(-1, -1)),
  WEST(new Vec2D(-1, 0)),
  NORTH_WEST(new Vec2D(-1, 1));

  private Vec2D key;

  public Vec2D getKey() {
    return key;
  }

  ShipGoalDirection(Vec2D key) {
    this.key = key;
  }

  public static ShipGoalDirection fromVec2D(Vec2D vec2D) {
    String vec2DString = "(" + vec2D.getX() + ", " + vec2D.getY() + ")";
    return switch (vec2DString) {
      case "(0, 1)" -> NORTH;
      case "(0, -1)" -> SOUTH;
      case "(-1, -1)" -> SOUTH_WEST;
      case "(-1, 0)" -> WEST;
      default -> null;
    };
  }

}
