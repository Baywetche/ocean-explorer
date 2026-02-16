package com.hhs.shipapp.connection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhs.shipapp.models.ShipMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShipClientConnection implements InitializingBean {

  private final String host;
  private final int port;
  private Socket toServer;
  private PrintWriter out;
  private BufferedReader in;
  private boolean connected = false;
  private final ObjectMapper mapper = new ObjectMapper();
  private static final Logger log = LoggerFactory.getLogger(ShipClientConnection.class);

  public ShipClientConnection(@Value("${ocean.server.host:localhost}") String host, @Value("${ocean.server.port:8150}") int port) {
    this.host = host;
    this.port = port;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    connect();
  }

  public void connect() {
    try {
      toServer = new Socket(host, port);
      in = new BufferedReader(new InputStreamReader(toServer.getInputStream()));
      out = new PrintWriter(toServer.getOutputStream(), true);
      log.info("Connected to {}:{}", host, port);

      connected = true;
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

  public List<String> receiveMessagesFromServer() {

    List<String> responses = new ArrayList<>();
    if (!connected || in == null) {
      log.warn("No active connection to server");
      return responses;
    }

    try {
      while (in.ready()) {
        String line = in.readLine();
        if (line == null) {
          log.warn("Server closed the connection");
          connected = false;
          break;
        }

        line = line.trim();
        if (line.isEmpty())
          continue;
        responses.add(line);

      }

      if (responses.isEmpty()) {
        log.debug("No new messages available");
      } else {
        log.debug("Read {} message(s) from server", responses.size());
      }

    } catch (IOException e) {
      log.error("Error while reading from server", e);
      connected = false;
    }

    return responses;
  }

  public void sendMessage2Server(ShipMessage message) {
    if (!connected) {
      log.warn("Not connected to server jet.");
      connect();
    }
    try {
      String jsonString = mapper.writeValueAsString(message);
      out.println(jsonString);
      log.info("Sent : {}", jsonString);
    } catch (Exception e) {
      log.error("Failed to send message to server", e);
      connected = false;
    }
  }

  public boolean isConnected(){
    return connected && toServer != null;
  }

  public boolean closeConnection() {
    connected = false;

    try {
      if (out != null)
        out.close();
      log.info("""
          PrintWriter closed successfully!
          """);
      if (in != null)
        in.close();
      log.info("""
          BufferedReader closed successfully!
          """);
      if (toServer != null)
        toServer.close();
      log.info("""
          Connection to Ship-Server closed successfully!
          """);

      return true;
    } catch (Exception e) {
      return false;
    }
  }

}