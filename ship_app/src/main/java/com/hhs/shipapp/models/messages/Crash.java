package com.hhs.shipapp.models.messages;

import com.hhs.lib.model.Sector;
import com.hhs.shipapp.models.SunkPos;
import com.hhs.shipapp.models.enums.Commands;
import com.hhs.shipapp.models.enums.MessageType;
import lombok.Data;

@Data
public class Crash {
  private Commands cmd;
  private String id;
  private MessageType type;
  private Sector sector;
  private SunkPos sunkPos;
}
