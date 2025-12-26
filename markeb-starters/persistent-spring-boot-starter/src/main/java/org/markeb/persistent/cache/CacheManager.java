package org.markeb.persistent.cache;

import java.time.Duration;
import java.util.Optional;

/**
 * 缓存管理器接口
 */
public interface CacheManager {

    /**
     * 获取缓存类型
     */
    CacheType getType();

    /**
     * 获取缓存值
     *
     * @param key  缓存键
     * @param type 值类型
     * @return 缓存值（可能为空）
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * 设置缓存值（使用默认 TTL）
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    <T> void put(String key, T value);

    /**
     * 设置缓存值（指定 TTL）
     *
     * @param key   缓存键
     * @param value 缓存值
     * @param ttl   过期时间
     */
    <T> void put(String key, T value, Duration ttl);

    /**
     * 删除缓存
     *
     * @param key 缓存键
     */
    void evict(String key);

    /**
     * 判断缓存是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    boolean exists(String key);

    /**
     * 清空所有缓存
     */
    void clear();

    /**
     * 获取并删除缓存
     *
     * @param key  缓存键
     * @param type 值类型
     * @return 缓存值（可能为空）
     */
    default <T> Optional<T> getAndEvict(String key, Class<T> type) {
        Optional<T> value = get(key, type);
        evict(key);
        return value;
    }
}
