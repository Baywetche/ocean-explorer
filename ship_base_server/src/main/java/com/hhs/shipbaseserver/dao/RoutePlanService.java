package com.hhs.shipbaseserver.dao;

import com.hhs.lib.model.RoutePlan;
import com.hhs.shipbaseserver.repository.RoutePlanRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class RoutePlanService {

  private final RoutePlanRepository routePlanRepository;

  public RoutePlanService(RoutePlanRepository routePlanRepository) {
    this.routePlanRepository = routePlanRepository;
  }

  public ResponseEntity<Boolean> save(RoutePlan routePlan) {

    routePlanRepository.save(routePlan);

    return ResponseEntity.ok(true);
  }

  public ResponseEntity<Boolean> deleteAllRoutePlan() {

    routePlanRepository.deleteAll();

    return ResponseEntity.ok(true);
  }

}
