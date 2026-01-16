package com.hhs.ocean_explorer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsolutePosition {
  private int[] vec2;
  public AbsolutePosition(Vec2D vec2d) {
    vec2 = new int[]{ vec2d.getX(), vec2d.getY()};
  }
}
