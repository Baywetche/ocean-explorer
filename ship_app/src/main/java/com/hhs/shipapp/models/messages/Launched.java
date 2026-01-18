package com.hhs.shipapp.models.messages;

import com.hhs.shipapp.models.AbsolutePosition;
import com.hhs.shipapp.models.enums.Commands;
import lombok.Data;

@Data
public class Launched {
  private Commands cmd;
  private String id;
  private AbsolutePosition abspos;
}
