package com.hhs.shipapp.models;

import com.hhs.lib.model.Vec2D;
import com.hhs.shipapp.models.enums.Course;
import com.hhs.shipapp.models.enums.Rudder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipMoveEvent {
  private Vec2D position;
  private Vec2D direction;
  private Course course;
  private Rudder rudder;
}