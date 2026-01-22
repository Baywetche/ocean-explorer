package com.hhs.shipapp.config;

import com.hhs.shipapp.models.ShipEntityState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ShipStateConfig {

  @Bean
  public Map<String, ShipEntityState> shipGameStates() {
    return new ConcurrentHashMap<>();
  }
}
