package com.hhs.shipapp.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sector {

  private int[] vec2;

  public Sector(Vec2D vec) {
    vec2 = new int[] { vec.getX(), vec.getY() };
  }
}
