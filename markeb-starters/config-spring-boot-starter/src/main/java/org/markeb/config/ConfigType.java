package org.markeb.config;

/**
 * 配置中心类型
 */
public enum ConfigType {

    /**
     * Nacos 配置中心
     */
    NACOS,

    /**
     * Etcd 配置中心
     */
    ETCD,

    /**
     * Consul 配置中心
     */
    CONSUL,

    /**
     * 本地文件（用于测试）
     */
    LOCAL
}

