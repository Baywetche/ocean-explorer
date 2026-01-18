package com.hhs.shipapp.service;

import com.hhs.lib.model.Vec2D;
import com.hhs.shipapp.models.ShipMessage;

import java.util.List;

public interface ShipApp {

  List<ShipMessage> launch(String name, Vec2D sector, Vec2D direction);

  List<ShipMessage> navigate(String rudder, String course);

  void exit();

  List<ShipMessage> scan();

  List<ShipMessage> radar();

}
