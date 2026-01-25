package com.hhs.shipapp.service;

import com.hhs.lib.model.SectorData;
import com.hhs.lib.model.ShipData;
import com.hhs.lib.model.Vec2D;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ShipTransportMessage {

  private final String shipBaseServerAPI = "http://localhost:8090/shipBaseServerAPI";
  private final int shipBasePort = 8090;

  public void saveSectorData(SectorData sectorData) {
    RestClient restClient = RestClient.create();

    String response = restClient.post().uri(shipBaseServerAPI + "/saveSectorData").body(sectorData).retrieve()
                                .body(String.class);

    System.out.println("Antwort vom ShipBaseServer: " + "saving sectorData successful: " + response);
  }

  public boolean updateSectorData(SectorData sectorData) {
    RestClient restClient = RestClient.create();
    boolean response = Boolean.TRUE.equals(
        restClient.put().uri(shipBaseServerAPI + "/updateSectorData").body(sectorData).retrieve().body(boolean.class));

    System.out.println("Antwort vom ShipBaseServer: " + "updating sectorData successful: " + response);

    return response;
  }

  public boolean isSectorFree(Vec2D sector) {
    RestClient restClient = RestClient.create();

    Boolean response = restClient.get().uri(uriBuilder -> uriBuilder.scheme("http").host("localhost").port(shipBasePort)
                                                                    .path("/shipBaseServerAPI/findIfSectorInUse")
                                                                    .queryParam("sectorX", sector.getX())
                                                                    .queryParam("sectorY", sector.getY()).build())
                                 .retrieve().body(Boolean.class);

    return Boolean.TRUE.equals(response);
  }

  public boolean isSectorDataExist(SectorData sectorData) {
    RestClient restClient = RestClient.create();
    boolean response = Boolean.TRUE.equals(
        restClient.get().uri(shipBaseServerAPI + "/findSectorData").retrieve().body(boolean.class));

    System.out.println("Antwort vom ShipBaseServer: " + "sector exist: " + response);

    return response;
  }


  /* about ship data */
  public boolean saveShipData(ShipData shipData) {
    RestClient restClient = RestClient.create();
    boolean response = Boolean.TRUE.equals(
        restClient.post().uri(shipBaseServerAPI + "/saveShipData").body(shipData).retrieve().body(boolean.class));

    System.out.println("Antwort vom ShipBaseServer: " + "saving shipData successful: " + response);

    return response;
  }

  public boolean updateShipData(ShipData shipData) {
    RestClient restClient = RestClient.create();
    boolean response = Boolean.TRUE.equals(
        restClient.put().uri(shipBaseServerAPI + "/updateShipData").body(shipData).retrieve().body(boolean.class));

    System.out.println("Antwort vom ShipBaseServer: " + "updating shipData successful: " + response);

    return response;
  }

  public ShipData getShipData(String shipId) {
    RestClient restClient = RestClient.create();
    ShipData response = restClient.get()
                                  .uri(uriBuilder -> uriBuilder.scheme("http").host("localhost").port(shipBasePort)
                                                               .path("/shipBaseServerAPI/getShipData")
                                                               .queryParam("shipId", shipId)
                                                               .build())
                                  .retrieve().body(ShipData.class);

    System.out.println("Antwort vom ShipBaseServer: " + "searching for shipData successful: " + response.toString());

    return response;
  }

  public boolean removeShipData(String shipId) {
    RestClient restClient = RestClient.create();

    Boolean response = restClient
        .delete()
        .uri(shipBaseServerAPI + "/deleteShipData?shipId={shipId}", shipId)
        .retrieve()
        .body(Boolean.class);

    System.out.println("Antwort vom ShipBaseServer: delete shipData successful: " + response);
    return Boolean.TRUE.equals(response);
  }

}
