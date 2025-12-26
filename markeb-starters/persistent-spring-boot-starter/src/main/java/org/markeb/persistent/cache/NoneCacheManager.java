package org.markeb.persistent.cache;

import java.time.Duration;
import java.util.Optional;

/**
 * 无缓存实现（空实现）
 * 用于禁用缓存的场景
 */
public class NoneCacheManager implements CacheManager {

    @Override
    public CacheType getType() {
        return CacheType.NONE;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        return Optional.empty();
    }

    @Override
    public <T> void put(String key, T value) {
        // no-op
    }

    @Override
    public <T> void put(String key, T value, Duration ttl) {
        // no-op
    }

    @Override
    public void evict(String key) {
        // no-op
    }

    @Override
    public boolean exists(String key) {
        return false;
    }

    @Override
    public void clear() {
        // no-op
    }
}

