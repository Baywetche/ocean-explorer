package com.hhs.shipapp.models.messages;

import com.hhs.shipapp.models.AbsolutePosition;
import com.hhs.shipapp.models.Direction;
import com.hhs.lib.model.Sector;
import com.hhs.shipapp.models.enums.Commands;
import lombok.Data;

@Data
public class Move2d {
  private Commands cmd;
  private String id;
  private Sector sector;
  private Direction dir;
  private AbsolutePosition abspos;
}