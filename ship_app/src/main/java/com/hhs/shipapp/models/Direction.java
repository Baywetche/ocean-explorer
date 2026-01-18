package com.hhs.shipapp.models;

import com.hhs.lib.model.Vec2D;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Direction {
  private int[] vec2;

  public Direction(Vec2D vec2d) {
    this.vec2 = new int[]{vec2d.getX(),vec2d.getY()};
  }
}
