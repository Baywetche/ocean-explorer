package com.hhs.shipapp.models;

import com.hhs.lib.model.Vec3D;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SunkPos {
  private int[] vec;
  public SunkPos(Vec3D vec3D) {
    vec = new int[]{ vec3D.getX(), vec3D.getY(), vec3D.getZ()};
  }
}
