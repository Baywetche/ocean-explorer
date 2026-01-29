package com.hhs.shipapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  public RestClient shipBaseRestClient() {
    return RestClient.builder()
        .baseUrl("http://localhost:8090/shipBaseServerAPI")
        .build();
  }

}
