package com.hhs.shipapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hhs.lib.model.Sector;
import com.hhs.lib.model.Vec2D;
import com.hhs.shipapp.connection.ShipClientConnection;
import com.hhs.shipapp.models.Direction;
import com.hhs.shipapp.models.ShipMessage;
import com.hhs.shipapp.models.enums.Commands;
import com.hhs.shipapp.models.enums.Course;
import com.hhs.shipapp.models.enums.Rudder;
import com.hhs.shipapp.models.enums.Typ;
import com.hhs.shipapp.util.ResponseManager;
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
  public List<ShipMessage> launch(String name, Vec2D sector, Vec2D shipDirection) {
    messages = new ArrayList<>();
    ShipMessage msg =
        ShipMessage.builder().cmd(Commands.launch).name(name).sector(new Sector(sector)).typ(Typ.ship).dir(new Direction(shipDirection))
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
  public List<ShipMessage> navigate(String course, String rudder) {
    messages = new ArrayList<>();
    ShipMessage msg = ShipMessage.builder().cmd(Commands.navigate).course(getCourse(course)).rudder(getRudder(rudder)).build();
    clientConnection.sendMessage2Server(msg);

    try {
      Thread.sleep(200);
      List<String> responses = clientConnection.receiveMessagesFromServer();
      messages.add(!responses.getFirst().contains("\"cmd\":\"crash\"")
          ? responseManager.move2dResponse(responses.getFirst())
          : responseManager.crashResponse(responses.getFirst()));
    } catch (InterruptedException | JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return messages;
  }

  private Course getCourse(String course) {
    return switch (course) {
      case "Forward" -> Course.Forward;
      case "Backward" -> Course.Backward;
      default -> throw new IllegalStateException("Unexpected course: " + course);
    };
  }

  private Rudder getRudder(String rudder) {
    return switch (rudder) {
      case "Center" -> Rudder.Center;
      case "Right" -> Rudder.Right;
      case "Left" -> Rudder.Left;
      default -> throw new IllegalStateException("Unexpected rudder: " + rudder);
    };
  }

  @Override
  public List<ShipMessage> radar() {
    messages = new ArrayList<>();
    ShipMessage msg = ShipMessage.builder().cmd(Commands.radar).build();
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
    ShipMessage msg = ShipMessage.builder().cmd(Commands.scan).build();
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
    ShipMessage msg = ShipMessage.builder().cmd(Commands.exit).build();
    clientConnection.sendMessage2Server(msg);
  }
}