package com.hhs.ocean_explorer.models.messages;

import com.hhs.ocean_explorer.models.Sector;
import com.hhs.ocean_explorer.models.SunkPos;
import com.hhs.ocean_explorer.models.enums.Commands;
import com.hhs.ocean_explorer.models.enums.MessageType;
import lombok.Data;

@Data
public class Crash {
  private Commands cmd;
  private String id;
  private MessageType type;
  private Sector sector;
  private SunkPos sunkPos;
}
