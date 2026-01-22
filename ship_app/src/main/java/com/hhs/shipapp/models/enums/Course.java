package com.hhs.shipapp.models.enums;

public enum Course {
  Forward("Forward"),
  Backward("Backward");

  private String key;

  Course(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public static Course fromString(String key) {
    return switch (key) {
      case "Forward" -> Course.Forward;
      case "Backward" -> Course.Backward;
      default -> null;
    };
  }
}
