package org.markeb.persistent.service;

import org.markeb.persistent.annotation.CacheConfig;
import org.markeb.persistent.annotation.PersistentEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 实体元数据
 * 解析实体类的注解配置
 */
public class EntityMetadata {

    private final Class<?> entityClass;
    private final String collection;
    private final boolean cached;
    private final Duration ttl;
    private final boolean asyncPersist;
    private final String keyPrefix;
    private final Duration localTtl;
    private final Duration remoteTtl;
    private final boolean twoLevel;

    private Method idGetter;
    private Method idSetter;
    private Field idField;

    public EntityMetadata(Class<?> entityClass) {
        this.entityClass = entityClass;

        // 解析 @PersistentEntity 注解
        PersistentEntity persistentEntity = entityClass.getAnnotation(PersistentEntity.class);
        if (persistentEntity != null) {
            this.collection = persistentEntity.collection().isEmpty()
                    ? entityClass.getSimpleName().toLowerCase()
                    : persistentEntity.collection();
            this.cached = persistentEntity.cacheEnabled();
            this.ttl = Duration.ofSeconds(persistentEntity.cacheTtl());
            this.asyncPersist = persistentEntity.asyncPersist();
        } else {
            this.collection = entityClass.getSimpleName().toLowerCase();
            this.cached = true;
            this.ttl = Duration.ofMinutes(30);
            this.asyncPersist = true;
        }

        // 解析 @CacheConfig 注解
        CacheConfig cacheConfig = entityClass.getAnnotation(CacheConfig.class);
        if (cacheConfig != null) {
            this.keyPrefix = cacheConfig.keyPrefix().isEmpty()
                    ? collection + ":"
                    : cacheConfig.keyPrefix();
            this.localTtl = Duration.ofSeconds(cacheConfig.localTtlSeconds());
            this.remoteTtl = Duration.ofSeconds(cacheConfig.remoteTtlSeconds());
            this.twoLevel = cacheConfig.twoLevel();
        } else {
            this.keyPrefix = collection + ":";
            this.localTtl = Duration.ofMinutes(5);
            this.remoteTtl = Duration.ofMinutes(30);
            this.twoLevel = false;
        }

        // 解析 ID 字段
        resolveIdAccessor();
    }

    private void resolveIdAccessor() {
        // 尝试找 getId 方法
        try {
            idGetter = entityClass.getMethod("getId");
            idSetter = entityClass.getMethod("setId", String.class);
            return;
        } catch (NoSuchMethodException ignored) {
        }

        // 尝试找 @Id 注解的字段
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(org.springframework.data.annotation.Id.class) ||
                    field.getName().equals("id")) {
                idField = field;
                idField.setAccessible(true);
                break;
            }
        }
    }

    /**
     * 获取实体ID
     */
    public String getId(Object entity) {
        try {
            if (idGetter != null) {
                return (String) idGetter.invoke(entity);
            } else if (idField != null) {
                return (String) idField.get(entity);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get entity id", e);
        }
        throw new RuntimeException("Cannot find id accessor for " + entityClass.getName());
    }

    /**
     * 设置实体ID
     */
    public void setId(Object entity, String id) {
        try {
            if (idSetter != null) {
                idSetter.invoke(entity, id);
            } else if (idField != null) {
                idField.set(entity, id);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set entity id", e);
        }
    }

    /**
     * 生成缓存键
     */
    public String cacheKey(String id) {
        return keyPrefix + id;
    }

    /**
     * 生成缓存键
     */
    public String cacheKey(Object entity) {
        return cacheKey(getId(entity));
    }

    // Getters

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getCollection() {
        return collection;
    }

    public boolean isCached() {
        return cached;
    }

    public Duration getTtl() {
        return ttl;
    }

    public boolean isAsyncPersist() {
        return asyncPersist;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public Duration getLocalTtl() {
        return localTtl;
    }

    public Duration getRemoteTtl() {
        return remoteTtl;
    }

    public boolean isTwoLevel() {
        return twoLevel;
    }

}

