package org.markeb.persistent.queue.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.markeb.persistent.queue.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka 持久化队列实现
 */
public class KafkaPersistentQueue implements PersistentQueue {

    private static final Logger log = LoggerFactory.getLogger(KafkaPersistentQueue.class);

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final String topic;
    private final ObjectMapper objectMapper;

    public KafkaPersistentQueue(KafkaTemplate<String, byte[]> kafkaTemplate,
                                 String topic,
                                 ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.objectMapper = objectMapper;
    }

    @Override
    public QueueType getType() {
        return QueueType.KAFKA;
    }

    @Override
    public CompletableFuture<Void> sendAsync(PersistentMessage message) {
        try {
            byte[] data = objectMapper.writeValueAsBytes(message);
            CompletableFuture<SendResult<String, byte[]>> future =
                    kafkaTemplate.send(topic, message.getEntityId(), data);

            return future.thenAccept(result ->
                    log.debug("Sent persistent message: {} -> {}, offset: {}",
                            message.getEntityClass(), message.getEntityId(),
                            result.getRecordMetadata().offset())
            ).exceptionally(ex -> {
                log.error("Failed to send persistent message: {} -> {}",
                        message.getEntityClass(), message.getEntityId(), ex);
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to serialize persistent message", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public void sendSync(PersistentMessage message) {
        try {
            byte[] data = objectMapper.writeValueAsBytes(message);
            kafkaTemplate.send(topic, message.getEntityId(), data).get();
            log.debug("Sent persistent message sync: {} -> {}",
                    message.getEntityClass(), message.getEntityId());
        } catch (Exception e) {
            log.error("Failed to send persistent message sync", e);
            throw new RuntimeException("Failed to send persistent message", e);
        }
    }

    @Override
    public void subscribe(PersistentMessageHandler handler) {
        // Kafka 的订阅通过 @KafkaListener 注解实现
        log.info("Kafka subscription should be configured via @KafkaListener");
    }

    @Override
    public void start() {
        log.info("Kafka persistent queue started with topic: {}", topic);
    }

    @Override
    public void stop() {
        log.info("Kafka persistent queue stopped");
    }
}
