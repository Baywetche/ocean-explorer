package com.hhs.ocean_explorer.models.messages;

import com.hhs.ocean_explorer.models.enums.Commands;
import com.hhs.ocean_explorer.models.enums.MessageType;
import lombok.Data;

@Data
public class Message {
  private Commands cmd;
  private MessageType type;
  private String text;
}
