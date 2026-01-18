package com.hhs.lib.model;


public enum ShipOrientation {
    n(new Vec2D(0, 1)),
    ne(new Vec2D(1, 1)),
    e(new Vec2D(1, 0)),
    se(new Vec2D(1, -1)),
    s(new Vec2D(0, -1)),
    sw(new Vec2D(-1, -1)),
    w(new Vec2D(-1, 0)),
    nw(new Vec2D(-1, 1));

    private final Vec2D key;

    ShipOrientation(Vec2D key) {
        this.key = key;
    }

    public Vec2D getKey() {
        return key;
    }

    public static ShipOrientation fromOrientation(Vec2D orientation) {
        for (ShipOrientation shipOrientation : values()) {
            if (shipOrientation.key.equals(orientation)) {
                return shipOrientation;
            }
        }

        throw new IllegalArgumentException("Unbekannte Orientierung: " + orientation);
    }
}
