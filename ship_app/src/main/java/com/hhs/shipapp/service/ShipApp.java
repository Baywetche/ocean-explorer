package com.hhs.shipapp.service;

import com.hhs.lib.model.Vec2D;
import com.hhs.shipapp.models.ShipMessage;

import java.util.List;

public interface ShipApp {

  List<ShipMessage> launch(String name, Vec2D sector, Vec2D direction);

  List<ShipMessage> navigate(String shipId, String rudder, String course);

  boolean exit(String shipId);

  List<ShipMessage> scan(String shipId);

  List<ShipMessage> radar(String shipId);

  boolean isClientConnectetToShipServer(String shipId);
}
