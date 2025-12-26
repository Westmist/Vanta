package org.markeb.id.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ID 生成器配置属性
 */
@ConfigurationProperties(prefix = "markeb.id")
public class IdGeneratorProperties {

    /**
     * 是否启用 ID 生成器
     */
    private boolean enabled = true;

    /**
     * WorkerId（机器码）
     * 范围取决于 workerIdBitLength，默认 6 位时范围是 0-63
     * 如果设置为 -1，则自动注册
     */
    private int workerId = -1;

    /**
     * WorkerId 位长度
     * 默认 6，取值范围 [1, 15]
     * 位数越大，支持的机器数越多，但 ID 长度也越长
     */
    private int workerIdBitLength = 6;

    /**
     * 序列号位长度
     * 默认 6，取值范围 [3, 21]
     * 位数越大，单位时间内生成的 ID 数量越多
     */
    private int seqBitLength = 6;

    /**
     * 基准时间（UTC）
     * 格式：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss
     * 不可修改，一旦设置就不能更改
     */
    private String baseTime = "2024-01-01";

    /**
     * 是否自动注册 WorkerId（通过 Redis）
     */
    private boolean autoRegister = false;

    /**
     * 自动注册配置
     */
    private AutoRegisterConfig autoRegisterConfig = new AutoRegisterConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getWorkerId() {
        return workerId;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }

    public int getWorkerIdBitLength() {
        return workerIdBitLength;
    }

    public void setWorkerIdBitLength(int workerIdBitLength) {
        this.workerIdBitLength = workerIdBitLength;
    }

    public int getSeqBitLength() {
        return seqBitLength;
    }

    public void setSeqBitLength(int seqBitLength) {
        this.seqBitLength = seqBitLength;
    }

    public String getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(String baseTime) {
        this.baseTime = baseTime;
    }

    public boolean isAutoRegister() {
        return autoRegister;
    }

    public void setAutoRegister(boolean autoRegister) {
        this.autoRegister = autoRegister;
    }

    public AutoRegisterConfig getAutoRegisterConfig() {
        return autoRegisterConfig;
    }

    public void setAutoRegisterConfig(AutoRegisterConfig autoRegisterConfig) {
        this.autoRegisterConfig = autoRegisterConfig;
    }

    /**
     * 自动注册配置
     */
    public static class AutoRegisterConfig {

        /**
         * Redis key 前缀
         */
        private String keyPrefix = "markeb:id:worker:";

        /**
         * WorkerId 最小值
         */
        private int minWorkerId = 0;

        /**
         * WorkerId 最大值（根据 workerIdBitLength 计算）
         * 默认 -1 表示自动计算
         */
        private int maxWorkerId = -1;

        /**
         * WorkerId 租约时间（秒）
         * 需要定期续租
         */
        private int leaseSeconds = 30;

        /**
         * 续租间隔（秒）
         * 建议为 leaseSeconds 的 1/3
         */
        private int renewIntervalSeconds = 10;

        public String getKeyPrefix() {
            return keyPrefix;
        }

        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        public int getMinWorkerId() {
            return minWorkerId;
        }

        public void setMinWorkerId(int minWorkerId) {
            this.minWorkerId = minWorkerId;
        }

        public int getMaxWorkerId() {
            return maxWorkerId;
        }

        public void setMaxWorkerId(int maxWorkerId) {
            this.maxWorkerId = maxWorkerId;
        }

        public int getLeaseSeconds() {
            return leaseSeconds;
        }

        public void setLeaseSeconds(int leaseSeconds) {
            this.leaseSeconds = leaseSeconds;
        }

        public int getRenewIntervalSeconds() {
            return renewIntervalSeconds;
        }

        public void setRenewIntervalSeconds(int renewIntervalSeconds) {
            this.renewIntervalSeconds = renewIntervalSeconds;
        }
    }
}

