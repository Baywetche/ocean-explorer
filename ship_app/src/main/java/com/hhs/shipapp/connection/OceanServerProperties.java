package com.hhs.shipapp.connection;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "ocean.server")
@Component
@Data
public class OceanServerProperties {
  private String host = "localhost";
  private int port = 8150;
}
