package com.hhs.ocean_explorer.service;

import com.hhs.ocean_explorer.models.ShipMessage;
import com.hhs.ocean_explorer.models.Vec2D;

import java.util.List;

public interface ShipApp {

  List<ShipMessage> launch(String name, Vec2D sector, Vec2D direction);

  List<ShipMessage> navigate(String rudder, String course);

  void exit();

  List<ShipMessage> scan();

  List<ShipMessage> radar();

}
