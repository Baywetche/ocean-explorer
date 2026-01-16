package com.hhs.ship_base_server.controller;

import com.hhs.ship_base_server.dao.SectorInfoDao;
import com.hhs.ship_base_server.model.SectorInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shipBaseServerAPI/message")
public class DataCollector {
  private SectorInfoDao sectorInfoDao;

  public DataCollector(SectorInfoDao sectorInfoDao){
    this.sectorInfoDao = sectorInfoDao;
  }

  @PostMapping
  public String receiveMassage(@RequestBody SectorInfo message) {
    System.out.println("Empfangen: " + message.toString());
    sectorInfoDao.save(message);
    return "OK";
  }

}
