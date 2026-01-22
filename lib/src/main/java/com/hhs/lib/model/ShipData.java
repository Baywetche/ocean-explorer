package com.hhs.lib.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ShipData {

    @Id
    private String shipId;

    private String shipName;
    private int sectorX;
    private int sectorY;
    private int directionX;
    private int directionY;

    public ShipData(String shipId, String shipName, int sectorX, int sectorY, int directionX, int directionY) {
        this.shipId = shipId;
        this.shipName = shipName;
        this.sectorX = sectorX;
        this.sectorY = sectorY;
        this.directionX = directionX;
        this.directionY = directionY;
    }

    public ShipData() {
    }
}
