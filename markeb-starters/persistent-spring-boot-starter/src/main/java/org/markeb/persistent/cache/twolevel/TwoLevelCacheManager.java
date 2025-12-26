package org.markeb.persistent.cache.twolevel;

import org.markeb.persistent.cache.CacheManager;
import org.markeb.persistent.cache.CacheType;
import org.markeb.persistent.cache.caffeine.CaffeineCacheManager;
import org.markeb.persistent.cache.redis.RedisCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;

/**
 * 两级缓存管理器实现
 * L1: Caffeine 本地缓存（快速访问）
 * L2: Redis 分布式缓存（数据共享）
 */
public class TwoLevelCacheManager implements CacheManager {

    private static final Logger log = LoggerFactory.getLogger(TwoLevelCacheManager.class);

    private final CaffeineCacheManager l1Cache;
    private final RedisCacheManager l2Cache;

    public TwoLevelCacheManager(CaffeineCacheManager l1Cache, RedisCacheManager l2Cache) {
        this.l1Cache = l1Cache;
        this.l2Cache = l2Cache;
    }

    @Override
    public CacheType getType() {
        return CacheType.TWO_LEVEL;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        // 先查 L1
        Optional<T> l1Value = l1Cache.get(key, type);
        if (l1Value.isPresent()) {
            log.debug("Cache hit L1: {}", key);
            return l1Value;
        }

        // L1 未命中，查 L2
        Optional<T> l2Value = l2Cache.get(key, type);
        if (l2Value.isPresent()) {
            log.debug("Cache hit L2: {}", key);
            // 回填 L1
            l1Cache.put(key, l2Value.get());
            return l2Value;
        }

        log.debug("Cache miss: {}", key);
        return Optional.empty();
    }

    @Override
    public <T> void put(String key, T value) {
        l1Cache.put(key, value);
        l2Cache.put(key, value);
    }

    @Override
    public <T> void put(String key, T value, Duration ttl) {
        l1Cache.put(key, value, ttl);
        l2Cache.put(key, value, ttl);
    }

    @Override
    public void evict(String key) {
        l1Cache.evict(key);
        l2Cache.evict(key);
    }

    @Override
    public boolean exists(String key) {
        return l1Cache.exists(key) || l2Cache.exists(key);
    }

    @Override
    public void clear() {
        l1Cache.clear();
        l2Cache.clear();
    }

    /**
     * 仅清除 L1 缓存
     */
    public void clearL1() {
        l1Cache.clear();
    }

    /**
     * 仅清除 L2 缓存
     */
    public void clearL2() {
        l2Cache.clear();
    }

    public CaffeineCacheManager getL1Cache() {
        return l1Cache;
    }

    public RedisCacheManager getL2Cache() {
        return l2Cache;
    }
}
