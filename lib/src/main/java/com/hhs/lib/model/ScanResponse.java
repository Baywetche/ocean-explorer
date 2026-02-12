package com.hhs.lib.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class ScanResponse {
  private int depth;
  private float stddev;

}
