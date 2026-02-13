package com.hhs.lib.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert ein 8-Richtungen-Koordinatensystem (Himmelsrichtungen) mit 45°-Schritten.
 * Vector bei Index 0 in List zeigt nach: Norden
 * Vector bei Index 1 in List zeigt nach: Nordosten
 * Vector bei Index 2 in List zeigt nach: Osten
 * Vector bei Index 3 in List zeigt nach: Südosten
 * Vector bei Index 4 in List zeigt nach: Süden
 * Vector bei Index 5 in List zeigt nach: Südwesten
 * Vector bei Index 6 in List zeigt nach: Westen
 * Vector bei Index 7 in List zeigt nach: Nordwesten
 */
public class RelativeCoordinateSystem {

  private List<Vec2D> coordinates;

  /**
   * Erzeugt ein neues Koordinatensystem, bei dem der übergebene Vektor zur neuen
   * Norden-Richtung (Index 0) wird.
   */
  public RelativeCoordinateSystem(Vec2D vec2D) {
    coordinates = new ArrayList<>(List.of(
        new Vec2D(0, 1),
        new Vec2D(1, 1),
        new Vec2D(1, 0),
        new Vec2D(1, -1),
        new Vec2D(0, -1),
        new Vec2D(-1, -1),
        new Vec2D(-1, 0),
        new Vec2D(-1, 1)
    ));

    int index = coordinates.indexOf(vec2D);
    if (index > 0) {
      List<Vec2D> rotated = coordinates.subList(index, coordinates.size());
      rotated.addAll(coordinates.subList(0, index));
      coordinates = new ArrayList<>(rotated);
    }
  }

  public List<Vec2D> getCoordinates() {
    return coordinates;
  }

  public DriveCommands calculateDriveCommandsForGoalDirection(Vec2D goalDirection) {
    int index = -1;
    for (int i = 0; i < coordinates.size(); i++) {
      Vec2D current = coordinates.get(i);
      if (current.getX() == goalDirection.getX() && current.getY() == goalDirection.getY()) {
        index = i;
        break;
      }
    }

    return switch (index) {
      case 0 -> DriveCommands.Forward_Center;
      case 1 -> DriveCommands.Forward_Right;
      case 7 -> DriveCommands.Forward_Left;
      case 4 -> DriveCommands.Backward_Center;
      case 3 -> DriveCommands.Backward_Right;
      case 5 -> DriveCommands.Backward_Left;
      default -> null;
    };
  }


}
