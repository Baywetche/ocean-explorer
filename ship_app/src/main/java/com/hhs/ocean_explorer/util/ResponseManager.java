package com.hhs.ocean_explorer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hhs.ocean_explorer.models.Echo;
import com.hhs.ocean_explorer.models.ShipMessage;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@Component
public class ResponseManager {

  ObjectMapper mapper = new ObjectMapper();

  public ShipMessage launchedResponse(String response) throws JsonProcessingException {
    ShipMessage launched = mapper.readValue(response, ShipMessage.class);
    return ShipMessage.builder()
        .cmd(launched.getCmd())
        .abspos(launched.getAbspos())
        .id(launched.getId())
        .build();
  }

  public ShipMessage move2dResponse(String response) throws JsonProcessingException {
    ShipMessage moved = mapper.readValue(response, ShipMessage.class);
    return ShipMessage.builder()
        .cmd(moved.getCmd())
        .id(moved.getId())
        .sector(moved.getSector())
        .dir(moved.getDir())
        .abspos(moved.getAbspos())
        .build();
  }

  public ShipMessage crashResponse(String jsonResponse) throws JsonProcessingException {
    ShipMessage response = mapper.readValue(jsonResponse, ShipMessage.class);
    return ShipMessage.builder()
        .cmd(response.getCmd())
        .id(response.getId())
        .message(response.getMessage())
        .sector(response.getSector())
        .sunkPos(response.getSunkPos())
        .build();
  }

  public ShipMessage messageResponse(String jsonResponse) throws JsonProcessingException {
    ShipMessage response = mapper.readValue(jsonResponse, ShipMessage.class);
    return ShipMessage.builder()
        .cmd(response.getCmd())
        .type(response.getType())
        .text(response.getText())
        .build();
  }

  public ShipMessage scanResponse(String jsonResponse) throws JsonProcessingException {
    ShipMessage response = mapper.readValue(jsonResponse, ShipMessage.class);
    return ShipMessage.builder()
        .cmd(response.getCmd())
        .id(response.getId())
        .depth(response.getDepth())
        .stddev(response.getStddev())
        .build();
  }

  public ShipMessage radarResponse(String jsonResponse) throws JsonProcessingException {
    ShipMessage response = mapper.readValue(jsonResponse, ShipMessage.class);
    return ShipMessage.builder()
          .cmd(response.getCmd())
          .id(response.getId())
          .echos(response.getEchos())
          .build();

  }

}