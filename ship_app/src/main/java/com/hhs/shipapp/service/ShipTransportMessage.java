package com.hhs.shipapp.service;

import com.hhs.lib.model.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class ShipTransportMessage {

  private final RestClient restClient;

  public ShipTransportMessage(RestClient restClient) {
    this.restClient = restClient;
  }

  public void saveSectorData(SectorData sectorData) {
    String response = restClient.post()
                                .uri("/saveSectorData")
                                .body(sectorData).retrieve().body(String.class);

//    System.out.println("Antwort vom ShipBaseServer: " + "saving sectorData successful: " + response);
  }

  public boolean updateSectorData(SectorData sectorData) {
    boolean response = Boolean.TRUE.equals(restClient.put()
                                                     .uri("/updateSectorData")
                                                     .body(sectorData).retrieve().body(boolean.class));

//    System.out.println("Antwort vom ShipBaseServer: " + "updating sectorData successful: " + response);

    return response;
  }

  public boolean isSectorFree(Vec2D sector) {
    Boolean response = restClient.get()
                                 .uri(uriBuilder -> uriBuilder.path("/findIfSectorInUse")
                                                              .queryParam("sectorX", sector.getX())
                                                              .queryParam("sectorY", sector.getY())
                                                              .build())
                                 .retrieve()
                                 .body(Boolean.class);

    return Boolean.TRUE.equals(response);
  }

  public boolean isSectorDataExist(SectorData sectorData) {
    boolean response = Boolean.TRUE.equals(restClient.get()
                                                     .uri("/findSectorData")
                                                     .retrieve().body(boolean.class));

    System.out.println("Antwort vom ShipBaseServer: " + "sector exist: " + response);

    return response;
  }

  /* about ship data */
  public boolean saveShipData(Ship ship) {
    boolean response = Boolean.TRUE.equals(restClient.post()
                                                     .uri("/saveShipData")
                                                     .body(ship).retrieve().body(boolean.class));

    System.out.println("Antwort vom ShipBaseServer: " + "saving shipData successful: " + response);

    return response;
  }

  public boolean updateShipData(Ship ship) {
    boolean response = Boolean.TRUE.equals(restClient.put()
                                                     .uri("/updateShipData")
                                                     .body(ship)
                                                     .retrieve().body(boolean.class));

//    System.out.println("Antwort vom ShipBaseServer: " + "updating shipData successful: " + response);

    return response;
  }

  public Ship getShipDataByShipId(String shipId) {
    Ship response = restClient.get().uri(
                                  uriBuilder -> uriBuilder.path("/getShipData")
                                                          .queryParam("shipId", shipId)
                                                          .build())
                              .retrieve().body(Ship.class);

    System.out.println("Antwort vom ShipBaseServer: " + "searching for shipData successful: " + response.toString());

    return response;
  }

  public boolean removeShipData(String shipId) {
    Boolean response = restClient.delete().uri(
                                     uriBuilder -> uriBuilder.path("/deleteShipData")
                                                             .queryParam("shipId", shipId)
                                                             .build())
                                 .retrieve().body(Boolean.class);

    System.out.println("Antwort vom ShipBaseServer: delete shipData by shipId: " + shipId + " successful: " + response);
    return Boolean.TRUE.equals(response);
  }

  public boolean existsShipIdInDB(String shipId) {
    Boolean response = restClient.get().uri(
                                     uriBuilder -> uriBuilder.path("/checkShipIdExists")
                                                             .queryParam("shipId", shipId)
                                                             .build())
                                 .retrieve().body(Boolean.class);

    System.out.println("Antwort vom ShipBaseServer: isShipIdExists: " + shipId + ": " + response);
    return Boolean.TRUE.equals(response);
  }

  /* about ship route */
  public boolean saveShipSector(ShipSector shipSector) {
    Boolean response = restClient.post()
                                .uri("/saveShipSector")
                                .body(shipSector).retrieve().body(Boolean.class);

//    System.out.println("Antwort vom ShipBaseServer: " + "saving ShipSector successful: " + response);

    return response.equals(Boolean.TRUE);
  }


  /* about ship autopilot */
  public boolean saveRoutePlan(RoutePlan routePlan) {
    boolean response = Boolean.TRUE.equals(restClient.post()
                                                     .uri("/saveRoutePlan")
                                                     .body(routePlan).retrieve().body(boolean.class));

    System.out.println("Antwort vom ShipBaseServer: " + "saving  RoutePlan successful: " + response);

    return response;
  }

  public boolean clearRoutePlan() {
    Boolean response = restClient.delete().uri(
                                     uriBuilder -> uriBuilder.path("/deleteAllRoutePlan")
                                                             .build())
                                 .retrieve().body(Boolean.class);

    System.out.println("Antwort vom ShipBaseServer: delete all route plan successful: " + response);
    return Boolean.TRUE.equals(response);
  }

  public Map<String, List<ShipSector>> getShipRoute() {
    Map<String, List<ShipSector>> response = restClient.get()
                                     .uri(uriBuilder -> uriBuilder.path("/getShipRoute")
                                                                  .build())
                                     .retrieve()
                                     .body(new ParameterizedTypeReference<>() {});

    return response;
  }

}

