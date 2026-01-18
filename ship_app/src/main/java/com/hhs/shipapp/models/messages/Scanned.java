package com.hhs.shipapp.models.messages;

import com.hhs.shipapp.models.enums.Commands;
import lombok.Data;

@Data
public class Scanned {
  private Commands cmd;
  private String id;
  private Integer depth;
  private Float stddev;
}
