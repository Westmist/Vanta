package org.markeb.persistent.consumer;

import org.markeb.persistent.cache.CacheManager;
import org.markeb.persistent.entity.Identifiable;
import org.markeb.persistent.queue.PersistentMessage;
import org.markeb.persistent.repository.Repository;
import org.markeb.persistent.repository.RepositoryFactory;
import org.markeb.persistent.service.EntityMetadataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 持久化消息消费者基类
 * 提供消息处理的通用逻辑，具体的消息监听由子类实现
 */
public abstract class PersistentMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(PersistentMessageConsumer.class);

    protected final RepositoryFactory repositoryFactory;
    protected final CacheManager cacheManager;
    protected final EntityMetadataRegistry metadataRegistry;

    public PersistentMessageConsumer(
            RepositoryFactory repositoryFactory,
            CacheManager cacheManager,
            EntityMetadataRegistry metadataRegistry) {
        this.repositoryFactory = repositoryFactory;
        this.cacheManager = cacheManager;
        this.metadataRegistry = metadataRegistry;
    }

    /**
     * 处理持久化消息
     */
    protected void handleMessage(PersistentMessage message) {
        log.info("Received persistent message: {}", message);

        try {
            Class<?> entityClass = resolveEntityClass(message);
            if (entityClass == null) {
                log.error("Cannot resolve entity class for message: {}", message);
                throw new RuntimeException("Cannot resolve entity class: " + message.getEntityClass());
            }

            switch (message.getType()) {
                case SAVE -> handleSave(message, entityClass);
                case DELETE -> handleDelete(message, entityClass);
                default -> log.warn("Unknown message type: {}", message.getType());
            }
        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
            throw new RuntimeException("Failed to process persistent message", e);
        }
    }

    /**
     * 处理保存消息
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void handleSave(PersistentMessage message, Class<?> entityClass) {
        String cacheKey = message.getCacheKey();

        // 从缓存获取实体
        var entityOpt = cacheManager.get(cacheKey, entityClass);
        if (entityOpt.isEmpty()) {
            log.error("Cache miss for key: {}", cacheKey);
            throw new RuntimeException("Cache miss for key: " + cacheKey);
        }

        // 保存到数据库
        Object entity = entityOpt.get();
        Repository repository = repositoryFactory.getRepository((Class<Identifiable>) entityClass);
        repository.save((Identifiable) entity);
        log.info("Saved entity to database: {}", cacheKey);
    }

    /**
     * 处理删除消息
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void handleDelete(PersistentMessage message, Class<?> entityClass) {
        Repository repository = repositoryFactory.getRepository((Class<Identifiable>) entityClass);
        repository.deleteById(entityClass, message.getId());
        log.info("Deleted entity from database: id={}", message.getId());
    }

    /**
     * 解析实体类
     */
    protected Class<?> resolveEntityClass(PersistentMessage message) {
        // 优先通过类名解析
        String className = message.getEntityClass();
        if (className != null && !className.isEmpty()) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                log.warn("Class not found: {}", className);
            }
        }

        // 通过集合名解析
        String collection = message.getCollection();
        if (collection != null && !collection.isEmpty()) {
            return metadataRegistry.getEntityClass(collection);
        }

        return null;
    }

}

