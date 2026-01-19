package com.hhs.shipapp.models;

import com.hhs.lib.model.RelativeCoordinateSystem;
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

  public static NavigableOrientation getNavigableOrientation(ShipMessage shipMessage, Vec2D sectorAtShipPosition, Vec2D shipDirection) {
    NavigableOrientation navigableOrientation = new NavigableOrientation();
    RelativeCoordinateSystem relativeCoordinateSystem = new RelativeCoordinateSystem(shipDirection);

    for (Echo echo : shipMessage.getEchos()) {
      Vec2D orientation = new Vec2D(echo.getSector().getVec2()[0] - sectorAtShipPosition.getX(),
          echo.getSector().getVec2()[1] - sectorAtShipPosition.getY()); // orientation z.B.: [0,-1]
      System.out.println(echo);

      /*       *//*Ausgabe*//*
            Vec2D orientationVec2d = new Vec2D(
                echo.getSector().getVec2()[0] - sectorAtShipPosition.getX(),
                echo.getSector().getVec2()[1] - sectorAtShipPosition.getY()
            );

            System.out.println(
                "Orientation | Echo(" + echo.getSector().getVec2()[0] + ", " + echo.getSector().getVec2()[1] +
                    ") - Ship(" + sectorAtShipPosition.getX() + ", " + sectorAtShipPosition.getY() +
                    ") => (" + orientationVec2d.getX() + ", " + orientationVec2d.getY() + ")"
            );
            *//*Ausgabeende*/

      int index = relativeCoordinateSystem.getCoordinates().indexOf(orientation);
      if (!isSectorNavigable(echo)) {
        switch (index) {
          case 0 -> {
            navigableOrientation.setN(false);
            //            System.out.println("navigable to: " + navigableOrientation.n + ": " + false);
          }
          case 1 -> {
            navigableOrientation.setNe(false);
            //            System.out.println("navigable to: " + navigableOrientation.ne + ": " + false);
          }
          case 2 -> {
            navigableOrientation.setE(false);
            //            System.out.println("navigable to: " + navigableOrientation.e + ": " + false);
          }
          case 3 -> {
            navigableOrientation.setSe(false);
            //            System.out.println("navigable to: " + navigableOrientation.se + ": " + false);
          }
          case 4 -> {
            navigableOrientation.setS(false);
            //            System.out.println("navigable to: " + navigableOrientation.s + ": " + false);
          }
          case 5 -> {
            navigableOrientation.setSw(false);
            //            System.out.println("navigable to: " + navigableOrientation.sw + ": " + false);
          }
          case 6 -> {
            navigableOrientation.setW(false);
            //            System.out.println("navigable to: " + navigableOrientation.w + ": " + false);
          }
          case 7 -> {
            navigableOrientation.setNw(false);
            //            System.out.println("navigable to: " + navigableOrientation.nw + ": " + false);
          }
        }
      }

    }

    return navigableOrientation;
  }

  private static boolean isSectorNavigable(Echo echo) {
    return echo.getHeight() <= 0 && (echo.getGround() == Ground.Water || echo.getGround() == Ground.Harbour);
  }

}

