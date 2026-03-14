package com.hhs.shipapp.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hhs.shipapp.models.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Lombok erzeugt Getter/Setter, Konstruktoren und Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShipMessage {

  private Commands cmd;
  private String id;
  private String name;
  private Typ typ;
  private Rudder rudder;
  private Course course;
  private Sector sector;
  private Direction dir;
  private MessageType type;
  private String message;
  private String text;
  private Integer depth;
  private AbsolutePosition abspos;
  private SunkPos sunkPos;
  private Float stddev;
  private List<Echo> echos;

}
