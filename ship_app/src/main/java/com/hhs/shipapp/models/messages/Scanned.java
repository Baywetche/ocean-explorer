package com.hhs.ocean_explorer.models.messages;

import com.hhs.ocean_explorer.models.enums.Commands;
import lombok.Data;

@Data
public class Scanned {
  private Commands cmd;
  private String id;
  private Integer depth;
  private Float stddev;
}
