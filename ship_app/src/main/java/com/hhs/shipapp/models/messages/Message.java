package com.hhs.shipapp.models.messages;

import com.hhs.shipapp.models.enums.Commands;
import com.hhs.shipapp.models.enums.MessageType;
import lombok.Data;

@Data
public class Message {
  private Commands cmd;
  private MessageType type;
  private String text;
}
