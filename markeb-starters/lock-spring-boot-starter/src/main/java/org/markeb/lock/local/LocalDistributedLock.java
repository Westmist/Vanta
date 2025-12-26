package org.markeb.lock.local;

import org.markeb.lock.DistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 本地锁实现（仅用于单机测试）
 */
public class LocalDistributedLock implements DistributedLock {

    private static final Logger log = LoggerFactory.getLogger(LocalDistributedLock.class);

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    private ReentrantLock getLock(String key) {
        return locks.computeIfAbsent(key, k -> new ReentrantLock());
    }

    @Override
    public void lock(String key) {
        ReentrantLock lock = getLock(key);
        lock.lock();
        log.debug("Acquired local lock: {}", key);
    }

    @Override
    public boolean tryLock(String key, long waitTime, TimeUnit unit) {
        try {
            ReentrantLock lock = getLock(key);
            boolean acquired = lock.tryLock(waitTime, unit);
            if (acquired) {
                log.debug("Acquired local lock: {}", key);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) {
        // 本地锁不支持自动释放，忽略 leaseTime
        return tryLock(key, waitTime, unit);
    }

    @Override
    public void unlock(String key) {
        ReentrantLock lock = locks.get(key);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("Released local lock: {}", key);
        }
    }

    @Override
    public boolean isLocked(String key) {
        ReentrantLock lock = locks.get(key);
        return lock != null && lock.isLocked();
    }

    @Override
    public boolean isHeldByCurrentThread(String key) {
        ReentrantLock lock = locks.get(key);
        return lock != null && lock.isHeldByCurrentThread();
    }
}

