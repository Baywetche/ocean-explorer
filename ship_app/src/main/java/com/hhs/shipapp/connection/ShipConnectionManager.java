package com.hhs.shipapp.connection;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Component
public class ShipConnectionManager {

  private final Map<String, ShipClientConnection> connections = new ConcurrentHashMap<>();

  private final OceanServerProperties oceanServerProperties;

  public ShipConnectionManager(OceanServerProperties oceanServerProperties) {
    this.oceanServerProperties = oceanServerProperties;
  }

  /**
   * Erstellt eine NEUE Verbindung zum Ocean-Server
   */
  public ShipClientConnection createNewConnection() {
    return new ShipClientConnection(
        oceanServerProperties.getHost(),
        oceanServerProperties.getPort()
    );
  }

  /**
   * Speichert eine Verbindung unter einem bestimmten Key (z. B. shipId oder Schiff-Name)
   */
  public void put(String key, ShipClientConnection connection) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Key darf nicht leer sein");
    }
    if (connection == null) {
      throw new IllegalArgumentException("Connection darf nicht null sein");
    }
    connections.put(key, connection);
  }

  /**
   * Holt die Verbindung zu einem bestimmten Schiff
   */
  public ShipClientConnection get(String key) {
    ShipClientConnection conn = connections.get(key);
    if (conn == null) {
      throw new IllegalArgumentException("Keine Verbindung für key: " + key);
    }
    return conn;
  }

  /**
   * Entfernt und gibt die Verbindung zurück (z. B. bei exit)
   */
  public ShipClientConnection remove(String key) {

    return connections.remove(key);
  }

  /**
   * Prüft, ob eine Verbindung für diesen Key existiert
   */
  public boolean exists(String key) {
    return connections.containsKey(key);
  }

  /**
   * Alle aktiven Keys (shipIds / Namen)
   */
  public Iterable<String> getAllKeys() {
    return connections.keySet();
  }

  /**
   * Schließt ALLE Verbindungen (z. B. beim Beenden der Anwendung)
   */
  public void closeAll() {
    for (ShipClientConnection conn : connections.values()) {
      try {
        conn.closeConnection();
      } catch (Exception e) {
        // stillschweigend ignorieren oder loggen
      }
    }
    connections.clear();
  }
}
