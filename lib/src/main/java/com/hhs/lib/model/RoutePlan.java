package com.hhs.lib.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class RoutePlan {

  @Id
  @GeneratedValue
  private Long id;

  @OneToMany(mappedBy = "routePlan", cascade = CascadeType.ALL, orphanRemoval = true)
  // Das cascade = ALL bedeutet, dass die RouteSteps nicht separat gespeichert werden muss. Wenn man den RoutePlan speichert, werden automatisch alle Steps mitgespeichert.
  @OrderBy("stepIndex ASC")
  private List<RouteStep> steps = new ArrayList<>();

  public void addStep(int x, int y) {
    RouteStep step = new RouteStep();
    step.setSectorX(x);
    step.setSectorY(y);

    step.setStepIndex(steps.size()); // <-- automatisch
    step.setRoutePlan(this);

    steps.add(step);
  }

}
