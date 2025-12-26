package org.markeb.eventbus.kafka;

import org.markeb.eventbus.Event;
import org.markeb.eventbus.EventBusType;
import org.markeb.eventbus.EventHandler;
import org.markeb.eventbus.EventSubscriber;
import org.markeb.eventbus.serialization.EventSerializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Kafka 事件订阅者实现
 */
public class KafkaEventSubscriber implements EventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventSubscriber.class);

    private final ConcurrentKafkaListenerContainerFactory<String, byte[]> containerFactory;
    private final EventSerializer eventSerializer;
    private final Map<String, ConcurrentMessageListenerContainer<String, byte[]>> containers = new ConcurrentHashMap<>();
    private final Map<String, EventHandler<?>> handlers = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public KafkaEventSubscriber(ConcurrentKafkaListenerContainerFactory<String, byte[]> containerFactory,
                                 EventSerializer eventSerializer) {
        this.containerFactory = containerFactory;
        this.eventSerializer = eventSerializer;
    }

    @Override
    public EventBusType getType() {
        return EventBusType.KAFKA;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void subscribe(String topic, EventHandler<?> handler) {
        handlers.put(topic, handler);

        ContainerProperties containerProperties = new ContainerProperties(topic);
        containerProperties.setMessageListener((MessageListener<String, byte[]>) record -> {
            try {
                Event event = eventSerializer.deserialize(record.value());
                ((EventHandler<Event>) handler).handle(event);
            } catch (Exception e) {
                log.error("Failed to handle Kafka event from topic: {}", topic, e);
            }
        });

        ConcurrentMessageListenerContainer<String, byte[]> container = 
                containerFactory.createContainer(topic);
        container.getContainerProperties().setMessageListener(
                (MessageListener<String, byte[]>) record -> {
                    try {
                        Event event = eventSerializer.deserialize(record.value());
                        ((EventHandler<Event>) handler).handle(event);
                    } catch (Exception e) {
                        log.error("Failed to handle Kafka event from topic: {}", topic, e);
                    }
                });

        containers.put(topic, container);

        if (running.get()) {
            container.start();
        }

        log.info("Subscribed to Kafka topic: {}", topic);
    }

    @Override
    public void unsubscribe(String topic) {
        ConcurrentMessageListenerContainer<String, byte[]> container = containers.remove(topic);
        if (container != null) {
            container.stop();
            handlers.remove(topic);
            log.info("Unsubscribed from Kafka topic: {}", topic);
        }
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            containers.values().forEach(ConcurrentMessageListenerContainer::start);
            log.info("Kafka event subscriber started");
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            containers.values().forEach(ConcurrentMessageListenerContainer::stop);
            log.info("Kafka event subscriber stopped");
        }
    }
}

