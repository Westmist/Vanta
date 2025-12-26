package org.markeb.lock;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁接口
 */
public interface DistributedLock {

    /**
     * 获取锁（阻塞）
     *
     * @param key 锁的key
     */
    void lock(String key);

    /**
     * 获取锁（带超时）
     *
     * @param key      锁的key
     * @param waitTime 等待时间
     * @param unit     时间单位
     * @return 是否获取成功
     */
    boolean tryLock(String key, long waitTime, TimeUnit unit);

    /**
     * 获取锁（带超时和自动释放时间）
     *
     * @param key       锁的key
     * @param waitTime  等待时间
     * @param leaseTime 持有锁的时间
     * @param unit      时间单位
     * @return 是否获取成功
     */
    boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit);

    /**
     * 释放锁
     *
     * @param key 锁的key
     */
    void unlock(String key);

    /**
     * 是否持有锁
     *
     * @param key 锁的key
     * @return 是否持有
     */
    boolean isLocked(String key);

    /**
     * 是否被当前线程持有
     *
     * @param key 锁的key
     * @return 是否被当前线程持有
     */
    boolean isHeldByCurrentThread(String key);
}

