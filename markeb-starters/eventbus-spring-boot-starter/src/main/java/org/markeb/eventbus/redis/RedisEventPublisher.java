package org.markeb.eventbus.redis;

import org.markeb.eventbus.Event;
import org.markeb.eventbus.EventBusType;
import org.markeb.eventbus.EventPublisher;
import org.markeb.eventbus.serialization.EventSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * Redis 事件发布者实现
 */
public class RedisEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RedisEventPublisher.class);

    private final RedisTemplate<String, byte[]> redisTemplate;
    private final EventSerializer eventSerializer;

    public RedisEventPublisher(RedisTemplate<String, byte[]> redisTemplate,
                                EventSerializer eventSerializer) {
        this.redisTemplate = redisTemplate;
        this.eventSerializer = eventSerializer;
    }

    @Override
    public EventBusType getType() {
        return EventBusType.REDIS;
    }

    @Override
    public void publish(Event event) {
        publish(event.topic(), event);
    }

    @Override
    public CompletableFuture<Void> publishAsync(Event event) {
        return publishAsync(event.topic(), event);
    }

    @Override
    public void publish(String topic, Event event) {
        try {
            byte[] data = eventSerializer.serialize(event);
            redisTemplate.convertAndSend(topic, data);
            log.debug("Published event to Redis topic: {}, event: {}", topic, event.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Failed to publish event to Redis topic: {}", topic, e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, Event event) {
        return CompletableFuture.runAsync(() -> publish(topic, event));
    }
}

