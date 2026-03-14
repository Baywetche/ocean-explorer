package com.hhs.shipapp.models.enums;

public enum DriveCommands {
  Forward_Center,
  Forward_Left,
  Forward_Right,
  Backward_Center,
  Backward_Left,
  Backward_Right;

  private String key;

  DriveCommands() {
  }

  public String getKey() {
    return key;
  }

}
