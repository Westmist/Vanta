package org.markeb.lock;

/**
 * 分布式锁类型
 */
public enum LockType {

    /**
     * Redis 实现（基于 Redisson）
     */
    REDIS,

    /**
     * Etcd 实现
     */
    ETCD,

    /**
     * 本地锁（仅用于单机测试）
     */
    LOCAL
}

