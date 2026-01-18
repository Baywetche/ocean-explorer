package com.hhs.ocean_explorer.models.messages;

import com.hhs.ocean_explorer.models.AbsolutePosition;
import com.hhs.ocean_explorer.models.enums.Commands;
import lombok.Data;

@Data
public class Launched {
  private Commands cmd;
  private String id;
  private AbsolutePosition abspos;
}
