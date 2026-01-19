package com.hhs.shipapp.service;

import com.hhs.lib.model.Vec2D;
import com.hhs.shipapp.models.SectorInfo;
import com.hhs.lib.model.Ground;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ShipTransportMessage {

  private final String shipBaseServerAPI = "http://localhost:8090/shipBaseServerAPI";

  public void sendMessage() {
    RestClient restClient = RestClient.create();

    String response = restClient.post()
        .uri(shipBaseServerAPI + "/saveSectorInfo")
        .body(new SectorInfo("#10#ship", Ground.Water, 4, 2, 3))
        .retrieve()
        .body(String.class);

    System.out.println("Antwort vom Server: " + response);
  }

  public boolean isSectorInfoExist(SectorInfo sectorInfo) {
    RestClient restClient = RestClient.create();
    boolean response = Boolean.TRUE.equals(restClient.post()
                                                     .uri(shipBaseServerAPI + "/findSectorInfo")
                                                     .body(sectorInfo)
                                                     .retrieve()
                                                     .body(boolean.class));

    System.out.println("Antwort vom Server: " + "sector exist: " + response);

    return response;
  }

  public boolean isSectorFree(Vec2D sector) {
    RestClient restClient = RestClient.create();
    boolean response = Boolean.TRUE.equals(restClient.post()
                                                     .uri(shipBaseServerAPI + "/findIfSectorInUse")
                                                     .body(sector)
                                                     .retrieve()
                                                     .body(boolean.class));

    System.out.println("Antwort vom Server: " + "sector exist: " + response);

    return response;
  }

}
