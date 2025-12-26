package org.markeb.eventbus.kafka;

import org.markeb.eventbus.Event;
import org.markeb.eventbus.EventBusType;
import org.markeb.eventbus.EventPublisher;
import org.markeb.eventbus.serialization.EventSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka 事件发布者实现
 */
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final EventSerializer eventSerializer;

    public KafkaEventPublisher(KafkaTemplate<String, byte[]> kafkaTemplate,
                                EventSerializer eventSerializer) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventSerializer = eventSerializer;
    }

    @Override
    public EventBusType getType() {
        return EventBusType.KAFKA;
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
            String key = event.key();
            
            if (key != null) {
                kafkaTemplate.send(topic, key, data).get();
            } else {
                kafkaTemplate.send(topic, data).get();
            }
            log.debug("Published event to Kafka topic: {}, event: {}", topic, event.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Failed to publish event to Kafka topic: {}", topic, e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, Event event) {
        byte[] data = eventSerializer.serialize(event);
        String key = event.key();

        CompletableFuture<SendResult<String, byte[]>> future;
        if (key != null) {
            future = kafkaTemplate.send(topic, key, data);
        } else {
            future = kafkaTemplate.send(topic, data);
        }

        return future.thenAccept(result -> 
                log.debug("Published event to Kafka topic: {}, offset: {}", 
                        topic, result.getRecordMetadata().offset()))
                .exceptionally(ex -> {
                    log.error("Failed to publish event to Kafka topic: {}", topic, ex);
                    return null;
                });
    }
}

