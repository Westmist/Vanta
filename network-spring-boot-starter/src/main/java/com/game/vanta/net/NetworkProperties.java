package com.game.vanta.net;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "network")
public class NetworkProperties {

  private int port = 9200;

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }
}
