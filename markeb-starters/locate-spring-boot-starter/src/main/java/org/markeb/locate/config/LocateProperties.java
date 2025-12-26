package org.markeb.locate.config;

import org.markeb.locate.LocateType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 定位服务配置属性
 */
@ConfigurationProperties(prefix = "markeb.locate")
public class LocateProperties {

    /**
     * 是否启用定位服务
     */
    private boolean enabled = true;

    /**
     * 定位服务类型
     */
    private LocateType type = LocateType.REDIS;

    /**
     * 位置信息过期时间
     */
    private Duration expireTime = Duration.ofMinutes(30);

    /**
     * 心跳间隔
     */
    private Duration heartbeatInterval = Duration.ofSeconds(30);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocateType getType() {
        return type;
    }

    public void setType(LocateType type) {
        this.type = type;
    }

    public Duration getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Duration expireTime) {
        this.expireTime = expireTime;
    }

    public Duration getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(Duration heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }
}

