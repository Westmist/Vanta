package org.markeb.eventbus.rocketmq;

import org.markeb.eventbus.Event;
import org.markeb.eventbus.EventBusType;
import org.markeb.eventbus.EventPublisher;
import org.markeb.eventbus.serialization.EventSerializer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * RocketMQ 事件发布者实现
 */
public class RocketMQEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RocketMQEventPublisher.class);

    private final RocketMQTemplate rocketMQTemplate;
    private final EventSerializer eventSerializer;

    public RocketMQEventPublisher(RocketMQTemplate rocketMQTemplate,
                                   EventSerializer eventSerializer) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.eventSerializer = eventSerializer;
    }

    @Override
    public EventBusType getType() {
        return EventBusType.ROCKETMQ;
    }

    @Override
    public void publish(Event event) {
        String destination = buildDestination(event.topic(), event.tag());
        publish(destination, event);
    }

    @Override
    public CompletableFuture<Void> publishAsync(Event event) {
        String destination = buildDestination(event.topic(), event.tag());
        return publishAsync(destination, event);
    }

    @Override
    public void publish(String topic, Event event) {
        try {
            byte[] data = eventSerializer.serialize(event);
            Message<byte[]> message = MessageBuilder.withPayload(data)
                    .setHeader("KEYS", event.key())
                    .build();
            
            rocketMQTemplate.syncSend(topic, message);
            log.debug("Published event to RocketMQ topic: {}, event: {}", topic, event.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Failed to publish event to RocketMQ topic: {}", topic, e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    @Override
    public CompletableFuture<Void> publishAsync(String topic, Event event) {
        return CompletableFuture.runAsync(() -> {
            try {
                byte[] data = eventSerializer.serialize(event);
                Message<byte[]> message = MessageBuilder.withPayload(data)
                        .setHeader("KEYS", event.key())
                        .build();
                
                rocketMQTemplate.asyncSend(topic, message, new org.apache.rocketmq.client.producer.SendCallback() {
                    @Override
                    public void onSuccess(org.apache.rocketmq.client.producer.SendResult sendResult) {
                        log.debug("Async published event to RocketMQ topic: {}", topic);
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("Failed to async publish event to RocketMQ topic: {}", topic, e);
                    }
                });
            } catch (Exception e) {
                log.error("Failed to publish event to RocketMQ topic: {}", topic, e);
            }
        });
    }

    /**
     * 构建目标地址 (topic:tag)
     */
    private String buildDestination(String topic, String tag) {
        if (tag != null && !tag.isEmpty() && !"*".equals(tag)) {
            return topic + ":" + tag;
        }
        return topic;
    }
}

