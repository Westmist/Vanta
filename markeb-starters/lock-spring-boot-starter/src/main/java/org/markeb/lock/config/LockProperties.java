package org.markeb.lock.config;

import org.markeb.lock.LockType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 分布式锁配置属性
 */
@ConfigurationProperties(prefix = "markeb.lock")
public class LockProperties {

    /**
     * 是否启用分布式锁
     */
    private boolean enabled = true;

    /**
     * 锁类型
     */
    private LockType type = LockType.REDIS;

    /**
     * 默认等待时间（秒）
     */
    private long defaultWaitTime = 3;

    /**
     * 默认持有时间（秒），-1 表示不自动释放
     */
    private long defaultLeaseTime = 30;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LockType getType() {
        return type;
    }

    public void setType(LockType type) {
        this.type = type;
    }

    public long getDefaultWaitTime() {
        return defaultWaitTime;
    }

    public void setDefaultWaitTime(long defaultWaitTime) {
        this.defaultWaitTime = defaultWaitTime;
    }

    public long getDefaultLeaseTime() {
        return defaultLeaseTime;
    }

    public void setDefaultLeaseTime(long defaultLeaseTime) {
        this.defaultLeaseTime = defaultLeaseTime;
    }
}

