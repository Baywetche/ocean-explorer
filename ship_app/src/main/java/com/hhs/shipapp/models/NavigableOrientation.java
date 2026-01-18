package com.hhs.shipapp.models;

import com.hhs.lib.model.ShipOrientation;
import com.hhs.lib.model.Vec2D;
import com.hhs.lib.model.Ground;
import lombok.Data;

@Data
public class NavigableOrientation {
    private boolean n = true;
    private boolean ne = true;
    private boolean e = false;
    private boolean se = true;
    private boolean s = true;
    private boolean sw = true;
    private boolean w = false;
    private boolean nw = true;


    public NavigableOrientation() {}


    public static NavigableOrientation getNavigableOrientation(ShipMessage shipMessage, Vec2D sectorAtShipPosition) {
        NavigableOrientation navigableOrientation = new NavigableOrientation();

        for (Echo echo : shipMessage.getEchos()) {
            Vec2D orientation = new Vec2D(echo.getSector().getVec2()[0] - sectorAtShipPosition.getX(),
                                          echo.getSector().getVec2()[1] - sectorAtShipPosition.getY());
            ShipOrientation shipOrientation = ShipOrientation.fromOrientation(orientation);

            if (!isSectorNavigable(echo)) {
                switch (shipOrientation) {
                    case n -> navigableOrientation.setN(false);
                    case ne -> navigableOrientation.setNe(false);
                    case e -> navigableOrientation.setE(false);
                    case se -> navigableOrientation.setSe(false);
                    case s -> navigableOrientation.setS(false);
                    case sw -> navigableOrientation.setSw(false);
                    case w -> navigableOrientation.setW(false);
                    case nw -> navigableOrientation.setNw(false);
                }
            }
        }

        return navigableOrientation;
    }

    private static boolean isSectorNavigable(Echo echo) {
        return echo.getHeight() <= 0 && (echo.getGround() == Ground.Water || echo.getGround() == Ground.Harbour);
    }

}

