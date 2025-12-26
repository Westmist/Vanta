package org.markeb.net.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 区服到后端地址映射配置： zoneId -> host:port
 */
@ConfigurationProperties(prefix = "gateway.backend")
public class GatewayBackendProperties {

    /**
     * 示例占位，实际部署请覆盖。
     */
    private Map<String, String> zones = new HashMap<>() {{
        put("zone1", "127.0.0.1:9000");
    }};

    public Map<String, String> getZones() {
        return zones;
    }

    public void setZones(Map<String, String> zones) {
        this.zones = zones;
    }
}

