package org.markeb.lock.redis;

import org.markeb.lock.DistributedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redisson 的分布式锁实现
 */
public class RedisDistributedLock implements DistributedLock {

    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLock.class);

    private static final String LOCK_PREFIX = "markeb:lock:";

    private final RedissonClient redissonClient;

    public RedisDistributedLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    private String getLockKey(String key) {
        return LOCK_PREFIX + key;
    }

    @Override
    public void lock(String key) {
        RLock lock = redissonClient.getLock(getLockKey(key));
        lock.lock();
        log.debug("Acquired lock: {}", key);
    }

    @Override
    public boolean tryLock(String key, long waitTime, TimeUnit unit) {
        try {
            RLock lock = redissonClient.getLock(getLockKey(key));
            boolean acquired = lock.tryLock(waitTime, unit);
            if (acquired) {
                log.debug("Acquired lock: {}", key);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Lock acquisition interrupted: {}", key);
            return false;
        }
    }

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        try {
            RLock lock = redissonClient.getLock(getLockKey(key));
            boolean acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (acquired) {
                log.debug("Acquired lock: {} with lease time: {} {}", key, leaseTime, unit);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Lock acquisition interrupted: {}", key);
            return false;
        }
    }

    @Override
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(getLockKey(key));
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("Released lock: {}", key);
        }
    }

    @Override
    public boolean isLocked(String key) {
        RLock lock = redissonClient.getLock(getLockKey(key));
        return lock.isLocked();
    }

    @Override
    public boolean isHeldByCurrentThread(String key) {
        RLock lock = redissonClient.getLock(getLockKey(key));
        return lock.isHeldByCurrentThread();
    }
}

