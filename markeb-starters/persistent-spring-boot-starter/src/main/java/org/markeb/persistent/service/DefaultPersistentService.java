package org.markeb.persistent.service;

import org.markeb.persistent.annotation.PersistentEntity;
import org.markeb.persistent.cache.CacheManager;
import org.markeb.persistent.entity.Identifiable;
import org.markeb.persistent.queue.PersistentMessage;
import org.markeb.persistent.queue.PersistentQueue;
import org.markeb.persistent.repository.Repository;
import org.markeb.persistent.serialization.EntitySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * 默认持久化服务实现
 */
public class DefaultPersistentService implements PersistentService {

    private static final Logger log = LoggerFactory.getLogger(DefaultPersistentService.class);

    private final Repository<Identifiable<Object>, Object> repository;
    private final CacheManager cacheManager;
    private final PersistentQueue persistentQueue;
    private final EntitySerializer entitySerializer;
    private final Duration defaultCacheTtl;

    @SuppressWarnings("unchecked")
    public DefaultPersistentService(Repository<?, ?> repository,
                                     CacheManager cacheManager,
                                     PersistentQueue persistentQueue,
                                     EntitySerializer entitySerializer,
                                     Duration defaultCacheTtl) {
        this.repository = (Repository<Identifiable<Object>, Object>) repository;
        this.cacheManager = cacheManager;
        this.persistentQueue = persistentQueue;
        this.entitySerializer = entitySerializer;
        this.defaultCacheTtl = defaultCacheTtl != null ? defaultCacheTtl : Duration.ofMinutes(30);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Identifiable<ID>, ID> Optional<T> find(Class<T> clazz, ID id) {
        String cacheKey = buildCacheKey(clazz, id);

        // 先查缓存
        Optional<T> cached = cacheManager.get(cacheKey, clazz);
        if (cached.isPresent()) {
            log.debug("Cache hit: {} -> {}", clazz.getSimpleName(), id);
            return cached;
        }

        // 缓存未命中，查数据库
        log.debug("Cache miss: {} -> {}", clazz.getSimpleName(), id);
        Optional<T> entity = (Optional<T>) repository.findById((Class<Identifiable<Object>>) clazz, id);

        // 回填缓存
        entity.ifPresent(e -> {
            Duration ttl = getCacheTtl(clazz);
            cacheManager.put(cacheKey, e, ttl);
        });

        return entity;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Identifiable<ID>, ID> T save(T entity) {
        // 保存到数据库
        T saved = (T) repository.save((Identifiable<Object>) entity);

        // 更新缓存
        String cacheKey = buildCacheKey(entity.getClass(), entity.getId());
        Duration ttl = getCacheTtl(entity.getClass());
        cacheManager.put(cacheKey, saved, ttl);

        log.debug("Saved entity: {} -> {}", entity.getClass().getSimpleName(), entity.getId());
        return saved;
    }

    @Override
    public <T extends Identifiable<ID>, ID> void saveAsync(T entity) {
        // 先更新缓存
        String cacheKey = buildCacheKey(entity.getClass(), entity.getId());
        Duration ttl = getCacheTtl(entity.getClass());
        cacheManager.put(cacheKey, entity, ttl);

        // 发送异步消息
        byte[] payload = entitySerializer.serialize(entity);
        PersistentMessage message = new PersistentMessage(
                PersistentMessage.MessageType.SAVE,
                entity.getClass().getName(),
                String.valueOf(entity.getId()),
                payload
        );

        persistentQueue.sendAsync(message)
                .exceptionally(ex -> {
                    log.error("Failed to send async save message: {} -> {}",
                            entity.getClass().getSimpleName(), entity.getId(), ex);
                    return null;
                });

        log.debug("Async save entity: {} -> {}", entity.getClass().getSimpleName(), entity.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Identifiable<ID>, ID> void delete(T entity) {
        // 删除数据库
        repository.delete((Identifiable<Object>) entity);

        // 删除缓存
        String cacheKey = buildCacheKey(entity.getClass(), entity.getId());
        cacheManager.evict(cacheKey);

        log.debug("Deleted entity: {} -> {}", entity.getClass().getSimpleName(), entity.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Identifiable<ID>, ID> void deleteById(Class<T> clazz, ID id) {
        // 删除数据库
        repository.deleteById((Class<Identifiable<Object>>) clazz, id);

        // 删除缓存
        String cacheKey = buildCacheKey(clazz, id);
        cacheManager.evict(cacheKey);

        log.debug("Deleted entity by id: {} -> {}", clazz.getSimpleName(), id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Identifiable<ID>, ID> List<T> findAll(Class<T> clazz) {
        return (List<T>) repository.findAll((Class<Identifiable<Object>>) clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Identifiable<ID>, ID> boolean exists(Class<T> clazz, ID id) {
        // 先查缓存
        String cacheKey = buildCacheKey(clazz, id);
        if (cacheManager.exists(cacheKey)) {
            return true;
        }
        // 再查数据库
        return repository.existsById((Class<Identifiable<Object>>) clazz, id);
    }

    @Override
    public <T extends Identifiable<ID>, ID> Optional<T> refresh(Class<T> clazz, ID id) {
        // 先清除缓存
        evictCache(clazz, id);
        // 重新查询
        return find(clazz, id);
    }

    @Override
    public <T extends Identifiable<ID>, ID> void evictCache(Class<T> clazz, ID id) {
        String cacheKey = buildCacheKey(clazz, id);
        cacheManager.evict(cacheKey);
        log.debug("Evicted cache: {} -> {}", clazz.getSimpleName(), id);
    }

    /**
     * 构建缓存键
     */
    private <T, ID> String buildCacheKey(Class<T> clazz, ID id) {
        String collection = getCollectionName(clazz);
        return collection + ":" + id;
    }

    /**
     * 获取集合/表名
     */
    private <T> String getCollectionName(Class<T> clazz) {
        PersistentEntity annotation = clazz.getAnnotation(PersistentEntity.class);
        if (annotation != null && !annotation.collection().isEmpty()) {
            return annotation.collection();
        }
        return clazz.getSimpleName().toLowerCase();
    }

    /**
     * 获取缓存 TTL
     */
    private <T> Duration getCacheTtl(Class<T> clazz) {
        PersistentEntity annotation = clazz.getAnnotation(PersistentEntity.class);
        if (annotation != null && annotation.cacheTtl() >= 0) {
            if (annotation.cacheTtl() == 0) {
                return Duration.ZERO; // 永不过期
            }
            return Duration.ofSeconds(annotation.cacheTtl());
        }
        return defaultCacheTtl;
    }
}
