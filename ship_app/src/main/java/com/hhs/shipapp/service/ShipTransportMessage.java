package com.hhs.shipapp.service;

import com.hhs.lib.model.SectorData;
import com.hhs.lib.model.ShipData;
import com.hhs.lib.model.Vec2D;

import com.hhs.lib.model.Ground;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ShipTransportMessage {

  private final String shipBaseServerAPI = "http://localhost:8090/shipBaseServerAPI";
  private final int shipBasePort = 8090;

  public void sendMessage() {
    RestClient restClient = RestClient.create();

    String response =
        restClient.post().uri(shipBaseServerAPI + "/saveSectorData").body(new SectorData("#10#ship", Ground.Water, 4, 2, 3)).retrieve()
            .body(String.class);

    System.out.println("Antwort vom Server: " + response);
  }

  public boolean isSectorFree(Vec2D sector) {
    RestClient restClient = RestClient.create();

    Boolean response = restClient.get()
        .uri(uriBuilder -> uriBuilder
            .scheme("http")
            .host("localhost")
            .port(shipBasePort)
            .path("/shipBaseServerAPI/findIfSectorInUse")
            .queryParam("sectorX", sector.getX())
            .queryParam("sectorY", sector.getY())
            .build())
        .retrieve()
        .body(Boolean.class);

    return Boolean.TRUE.equals(response);
  }

  public boolean isSectorInfoExist(SectorData sectorData) {
    RestClient restClient = RestClient.create();
    boolean response =
        Boolean.TRUE.equals(restClient.get().uri(shipBaseServerAPI + "/findSectorData").retrieve().body(boolean.class));

    System.out.println("Antwort vom Server: " + "sector exist: " + response);

    return response;
  }

  public boolean saveShipData(ShipData shipData) {
    RestClient restClient = RestClient.create();
    boolean response =
        Boolean.TRUE.equals(restClient.post().uri(shipBaseServerAPI + "/saveShipData").body(shipData).retrieve().body(boolean.class));

    System.out.println("Antwort vom Server: " + "saving shipData successful: " + response);

    return response;
  }

  public boolean updateShipData(ShipData shipData) {
    RestClient restClient = RestClient.create();
    boolean response =
        Boolean.TRUE.equals(restClient.post().uri(shipBaseServerAPI + "/updateShipData").body(shipData).retrieve().body(boolean.class));

    System.out.println("Antwort vom Server: " + "updating shipData successful: " + response);

    return response;
  }

}
