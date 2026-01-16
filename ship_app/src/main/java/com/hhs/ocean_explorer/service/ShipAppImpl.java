package com.hhs.ocean_explorer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hhs.ocean_explorer.connection.ShipClientConnection;
import com.hhs.ocean_explorer.models.Direction;
import com.hhs.ocean_explorer.models.Sector;
import com.hhs.ocean_explorer.models.ShipMessage;
import com.hhs.ocean_explorer.models.Vec2D;
import com.hhs.ocean_explorer.models.enums.Commands;
import com.hhs.ocean_explorer.models.enums.Course;
import com.hhs.ocean_explorer.models.enums.Rudder;
import com.hhs.ocean_explorer.models.enums.Typ;
import com.hhs.ocean_explorer.util.ResponseManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ShipAppImpl implements ShipApp {

  private ShipClientConnection clientConnection;
  private ResponseManager responseManager;
  private List<ShipMessage> messages;

  @Override
  public List<ShipMessage> launch(String name, Vec2D sector, Vec2D direction) {
    messages = new ArrayList<>();
    ShipMessage msg = ShipMessage.builder()
        .cmd(Commands.launch)
        .name(name)
        .sector(new Sector(sector))
        .typ(Typ.ship)
        .dir(new Direction(direction))
        .build();
    clientConnection.sendMessage2Server(msg);
    try {
      Thread.sleep(200);
      List<String> responses = clientConnection.receiveMessagesFromServer();
      ShipMessage launchedResponse = responseManager.launchedResponse(responses.getFirst());
      ShipMessage movedResponse = responseManager.move2dResponse(responses.getLast());

      messages.add(launchedResponse);
      messages.add(movedResponse);
    } catch (InterruptedException | JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return messages;
  }

  @Override
  public List<ShipMessage> navigate(String rudder, String course) {
    messages = new ArrayList<>();
    ShipMessage msg = ShipMessage.builder()
        .cmd(Commands.navigate)
        .rudder(Rudder.valueOf(rudder))
        .course(Course.valueOf(course))
        .build();
    clientConnection.sendMessage2Server(msg);

    try {
      Thread.sleep(200);
      List<String> responses = clientConnection.receiveMessagesFromServer();
      System.out.println(responses.getFirst());
      messages.add(
          !responses.getFirst().contains("\"cmd\":\"crash\"")
              ? responseManager.move2dResponse(responses.getFirst())
              : responseManager.crashResponse(responses.getFirst())
      );
    } catch (InterruptedException | JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return messages;
  }

  @Override
  public List<ShipMessage> radar() {
    messages = new ArrayList<>();
    ShipMessage msg = ShipMessage.builder()
        .cmd(Commands.radar)
        .build();
    clientConnection.sendMessage2Server(msg);
    try {
      Thread.sleep(200);
      List<String> responses = clientConnection.receiveMessagesFromServer();
      ShipMessage shipMessage = responseManager.radarResponse(responses.getFirst());
      messages.add(shipMessage);
    } catch (InterruptedException | JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return messages;
  }

  @Override
  public List<ShipMessage> scan() {
    messages = new ArrayList<>();
    ShipMessage msg = ShipMessage.builder()
        .cmd(Commands.scan)
        .build();
    clientConnection.sendMessage2Server(msg);

    try {
      Thread.sleep(200);
      List<String> responses = clientConnection.receiveMessagesFromServer();
      ShipMessage scanResponse = responseManager.scanResponse(responses.getFirst());
      messages.add(scanResponse);
    } catch (InterruptedException | JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return messages;
  }

  @Override
  public void exit() {
    ShipMessage msg = ShipMessage.builder()
        .cmd(Commands.exit)
        .build();
    clientConnection.sendMessage2Server(msg);
  }
}