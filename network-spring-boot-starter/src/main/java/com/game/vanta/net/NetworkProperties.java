package com.game.vanta.net;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "network")
public class NetworkProperties {

    private int port = 9200;

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
