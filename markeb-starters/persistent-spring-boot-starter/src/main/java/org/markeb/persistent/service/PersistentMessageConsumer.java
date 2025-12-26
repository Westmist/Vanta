package org.markeb.persistent.service;

import org.markeb.persistent.entity.Identifiable;
import org.markeb.persistent.queue.PersistentMessage;
import org.markeb.persistent.queue.PersistentMessageHandler;
import org.markeb.persistent.repository.Repository;
import org.markeb.persistent.serialization.EntitySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 持久化消息消费者
 * 处理异步持久化消息
 */
public class PersistentMessageConsumer implements PersistentMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(PersistentMessageConsumer.class);

    private final Repository<Identifiable<Object>, Object> repository;
    private final EntitySerializer entitySerializer;

    @SuppressWarnings("unchecked")
    public PersistentMessageConsumer(Repository<?, ?> repository,
                                      EntitySerializer entitySerializer) {
        this.repository = (Repository<Identifiable<Object>, Object>) repository;
        this.entitySerializer = entitySerializer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(PersistentMessage message) {
        log.debug("Processing persistent message: {} -> {}",
                message.getEntityClass(), message.getEntityId());

        try {
            Class<?> entityClass = Class.forName(message.getEntityClass());

            switch (message.getType()) {
                case SAVE -> handleSave(message, entityClass);
                case DELETE -> handleDelete(message, entityClass);
                default -> log.warn("Unknown message type: {}", message.getType());
            }
        } catch (ClassNotFoundException e) {
            log.error("Entity class not found: {}", message.getEntityClass(), e);
        } catch (Exception e) {
            log.error("Failed to process persistent message: {} -> {}",
                    message.getEntityClass(), message.getEntityId(), e);
            throw new RuntimeException("Failed to process persistent message", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleSave(PersistentMessage message, Class<?> entityClass) {
        if (message.getPayload() == null || message.getPayload().length == 0) {
            log.warn("Empty payload for save message: {} -> {}",
                    message.getEntityClass(), message.getEntityId());
            return;
        }

        Object entity = entitySerializer.deserialize(message.getPayload(), entityClass);
        repository.save((Identifiable<Object>) entity);
        log.debug("Saved entity from message: {} -> {}",
                message.getEntityClass(), message.getEntityId());
    }

    @SuppressWarnings("unchecked")
    private void handleDelete(PersistentMessage message, Class<?> entityClass) {
        // 尝试解析 ID
        String entityId = message.getEntityId();
        repository.deleteById((Class<Identifiable<Object>>) entityClass, entityId);
        log.debug("Deleted entity from message: {} -> {}",
                message.getEntityClass(), message.getEntityId());
    }
}

