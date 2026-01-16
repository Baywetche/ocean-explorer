package com.hhs.ocean_explorer.service;

import com.hhs.ocean_explorer.models.MessageDTO;
import com.hhs.ocean_explorer.models.SectorInfo;
import com.hhs.ocean_explorer.models.enums.Ground;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestClient;

@Service
public class ShipTransportMessage {

  private final String shipBaseServerAPI = "http://localhost:8090/shipBaseServerAPI";

  public void sendMessage() {
    RestClient restClient = RestClient.create();

    String response = restClient.post()
        .uri(shipBaseServerAPI + "/message")
        .body(new SectorInfo("#10#ship", Ground.Water, 4, 2, 3))
        .retrieve()
        .body(String.class);

    System.out.println("Antwort vom Server: " + response);
  }

}
