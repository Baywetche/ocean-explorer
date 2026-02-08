package com.hhs.shipapp.util;

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

}

























