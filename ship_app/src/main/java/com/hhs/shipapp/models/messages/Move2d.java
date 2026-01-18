package com.hhs.ocean_explorer.models.messages;

import com.hhs.ocean_explorer.models.AbsolutePosition;
import com.hhs.ocean_explorer.models.Direction;
import com.hhs.ocean_explorer.models.Sector;
import com.hhs.ocean_explorer.models.enums.Commands;
import lombok.Data;

@Data
public class Move2d {
  private Commands cmd;
  private String id;
  private Sector sector;
  private Direction dir;
  private AbsolutePosition abspos;
}