package com.hhs.lib.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@ToString
public class ShipMovementState {

  @Id
  private Long id;

  private int shipPositionSectorX;
  private int shipPositionSectorY;

  private int shipDirectionX;
  private int shipDirectionY;

  private int forbiddenSectorX;
  private int forbiddenSectorY;

  private int targetSectorX;
  private int targetSectorY;

  public ShipMovementState(int shipPositionSectorX, int shipPositionSectorY, int shipDirectionX, int shipDirectionY, int targetSectorX,
      int targetSectorY) {
    this.shipPositionSectorX = shipPositionSectorX;
    this.shipPositionSectorY = shipPositionSectorY;
    this.shipDirectionX = shipDirectionX;
    this.shipDirectionY = shipDirectionY;
    this.targetSectorX = targetSectorX;
    this.targetSectorY = targetSectorY;

    setForbiddenSector();
  }

  public ShipMovementState() {
    setForbiddenSector();
  }

  private void setForbiddenSector() {
    RelativeCoordinateSystem relativeCoordinateSystem = new RelativeCoordinateSystem(new Vec2D(shipDirectionX, shipDirectionY));

    forbiddenSectorX = relativeCoordinateSystem.getCoordinates().get(2).getX(); // Ost, also rechts vom Nord
    forbiddenSectorY = relativeCoordinateSystem.getCoordinates().get(6).getY(); // West, also links vom Nord
  }

}
