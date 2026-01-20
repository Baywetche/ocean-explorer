package com.hhs.shipbaseserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.hhs.lib.model")
@EnableJpaRepositories(basePackages = "com.hhs.shipbaseserver.repository")
public class ShipBaseServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ShipBaseServerApplication.class, args);
  }

}
