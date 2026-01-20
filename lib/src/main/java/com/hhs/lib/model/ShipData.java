package com.hhs.lib.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ShipData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String shipId;
    private String shipName;
    private int sectorX;
    private int sectorY;
    private int directionX;
    private int directionY;
}
