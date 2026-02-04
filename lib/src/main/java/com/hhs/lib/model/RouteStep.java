package com.hhs.lib.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class RouteStep {

  @Id
  @GeneratedValue
  private Long id;

  private int sectorX;
  private int sectorY;

  private int stepIndex;   // Reihenfolge!

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "route_plan_id", nullable = false)
  private RoutePlan routePlan;
}
