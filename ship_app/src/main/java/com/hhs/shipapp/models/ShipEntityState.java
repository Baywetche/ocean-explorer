package com.hhs.shipapp.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hhs.lib.model.Vec2D;
import com.hhs.shipapp.models.enums.Course;
import com.hhs.shipapp.models.enums.Rudder;
import com.hhs.shipapp.models.enums.ShipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)   // schützt vor unbekannten Feldern beim Deserialisieren
public class ShipEntityState {

  private String shipId;
  private String name;

  private Vec2D sector;
  private Vec2D direction;
  private Vec2D previousSector;

  private Course course;
  private Rudder rudder;

  private ShipStatus status;

  @Builder.Default
  private List<ShipMoveEvent> moveHistory = new ArrayList<>();

  public boolean isSunk() {
    return status == ShipStatus.SUNK;
  }

  public boolean isOperational() {
    return status == ShipStatus.ACTIVE && !isSunk();
  }

  public void recordMove(Vec2D newPosition, Course course, Rudder rudder) {
    this.previousSector = this.sector;
    this.sector = newPosition;
    this.course = course;
    this.rudder = rudder;

    // History optional befüllen
    this.moveHistory.add(new ShipMoveEvent(
        newPosition,
        direction,
        course,
        rudder
    ));
  }

  public void sink(String byShipId) {
    this.status = ShipStatus.SUNK;
  }

}