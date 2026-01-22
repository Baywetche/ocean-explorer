package com.hhs.shipapp.models.enums;

public enum Rudder {
  Left("Left"),    // links,Backbord, portside
  Center("Center"),  // geradeaus in gleicher Richtung
  Right("Right");    // rechts, Steuerbord, bowside

  private String key;

  Rudder(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public static Rudder fromString(String key) {
    return switch (key) {
      case "Left" -> Rudder.Left;
      case "Center" -> Rudder.Center;
      case "Right" -> Rudder.Right;
      default -> throw new IllegalStateException("Unexpected value: " + key);
    };
  }
}