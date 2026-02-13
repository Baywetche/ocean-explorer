package com.hhs.shipapp.util;

import com.hhs.lib.model.DriveCommands;

import java.security.PublicKey;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {

  public static String extractShipNameFromShipId(String shipId) {
    Pattern pattern = Pattern.compile("#\\d+#(.*)");
    Matcher matcher = pattern.matcher(shipId);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }

  public static String extractCourseFromDriveCommands(DriveCommands driveCommands) {
    return driveCommands.toString().split("_")[0];
  }

  public static String extractRudderFromDriveCommands(DriveCommands driveCommands) {
    return driveCommands.toString().split("_")[1];
  }

  public static void sleepMillis(long millis) {
    if (millis < 0) {
      throw new IllegalArgumentException("milles is can't be negative");
    }
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}

























