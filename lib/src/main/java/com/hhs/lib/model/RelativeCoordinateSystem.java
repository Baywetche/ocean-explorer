package com.hhs.lib.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert ein 8-Richtungen-Koordinatensystem (Himmelsrichtungen) mit 45°-Schritten.
 *   Vector bei Index 0 in List zeigt nach: Norden
 *   Vector bei Index 1 in List zeigt nach: Nordosten
 *   Vector bei Index 2 in List zeigt nach: Osten
 *   Vector bei Index 3 in List zeigt nach: Südosten
 *   Vector bei Index 4 in List zeigt nach: Süden
 *   Vector bei Index 5 in List zeigt nach: Südwesten
 *   Vector bei Index 6 in List zeigt nach: Westen
 *   Vector bei Index 7 in List zeigt nach: Nordwesten
 */
public class RelativeCoordinateSystem {

    private List<Vec2D> coordinates;


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
}
