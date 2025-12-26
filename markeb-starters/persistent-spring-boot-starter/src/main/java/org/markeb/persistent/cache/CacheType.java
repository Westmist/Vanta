package org.markeb.persistent.cache;

/**
 * 缓存类型枚举
 */
public enum CacheType {

    /**
     * Redis 缓存
     */
    REDIS,

    /**
     * Caffeine 本地缓存
     */
    CAFFEINE,

    /**
     * 两级缓存（本地 + Redis）
     */
    TWO_LEVEL,

    /**
     * 无缓存
     */
    NONE
}

