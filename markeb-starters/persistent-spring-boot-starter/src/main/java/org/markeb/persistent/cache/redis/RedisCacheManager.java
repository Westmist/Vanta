package org.markeb.persistent.cache.redis;

import org.markeb.persistent.cache.CacheManager;
import org.markeb.persistent.cache.CacheType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * Redis 缓存管理器实现
 */
public class RedisCacheManager implements CacheManager {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheManager.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final String keyPrefix;
    private final Duration defaultTtl;

    public RedisCacheManager(RedisTemplate<String, Object> redisTemplate,
                              String keyPrefix,
                              Duration defaultTtl) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix != null ? keyPrefix : "";
        this.defaultTtl = defaultTtl != null ? defaultTtl : Duration.ofMinutes(30);
    }

    @Override
    public CacheType getType() {
        return CacheType.REDIS;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            String fullKey = buildKey(key);
            Object value = redisTemplate.opsForValue().get(fullKey);
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
            String fullKey = buildKey(key);
            if (ttl == null || ttl.isZero() || ttl.isNegative()) {
                redisTemplate.opsForValue().set(fullKey, value);
            } else {
                redisTemplate.opsForValue().set(fullKey, value, ttl);
            }
        } catch (Exception e) {
            log.error("Failed to put cache: {}", key, e);
        }
    }

    @Override
    public void evict(String key) {
        try {
            String fullKey = buildKey(key);
            redisTemplate.delete(fullKey);
        } catch (Exception e) {
            log.error("Failed to evict cache: {}", key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            String fullKey = buildKey(key);
            return Boolean.TRUE.equals(redisTemplate.hasKey(fullKey));
        } catch (Exception e) {
            log.error("Failed to check cache existence: {}", key, e);
            return false;
        }
    }

    @Override
    public void clear() {
        try {
            Set<String> keys = redisTemplate.keys(keyPrefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("Failed to clear cache", e);
        }
    }

    private String buildKey(String key) {
        return keyPrefix + key;
    }

    public RedisTemplate<String, Object> getRedisTemplate() {
        return redisTemplate;
    }
}
