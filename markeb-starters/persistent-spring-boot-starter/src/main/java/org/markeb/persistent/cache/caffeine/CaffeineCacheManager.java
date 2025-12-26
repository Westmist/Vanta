package org.markeb.persistent.cache.caffeine;

import org.markeb.persistent.cache.CacheManager;
import org.markeb.persistent.cache.CacheType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine 本地缓存管理器实现
 */
public class CaffeineCacheManager implements CacheManager {

    private static final Logger log = LoggerFactory.getLogger(CaffeineCacheManager.class);

    private final Cache<String, Object> cache;
    private final Duration defaultTtl;

    public CaffeineCacheManager(long maxSize, Duration expireAfterWrite) {
        this.defaultTtl = expireAfterWrite;
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireAfterWrite.toMillis(), TimeUnit.MILLISECONDS)
                .recordStats()
                .build();
    }

    public CaffeineCacheManager(Cache<String, Object> cache, Duration defaultTtl) {
        this.cache = cache;
        this.defaultTtl = defaultTtl;
    }

    @Override
    public CacheType getType() {
        return CacheType.CAFFEINE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            Object value = cache.getIfPresent(key);
            if (value != null && type.isInstance(value)) {
                return Optional.of((T) value);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get cache: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public <T> void put(String key, T value) {
        put(key, value, defaultTtl);
    }

    @Override
    public <T> void put(String key, T value, Duration ttl) {
        try {
            // Caffeine 不支持单个 key 设置不同的 TTL
            // 如果需要不同 TTL，建议使用 Redis 或两级缓存
            cache.put(key, value);
        } catch (Exception e) {
            log.error("Failed to put cache: {}", key, e);
        }
    }

    @Override
    public void evict(String key) {
        try {
            cache.invalidate(key);
        } catch (Exception e) {
            log.error("Failed to evict cache: {}", key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        return cache.getIfPresent(key) != null;
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    public Cache<String, Object> getCache() {
        return cache;
    }
}
