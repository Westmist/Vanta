package org.markeb.eventbus.rocketmq;

import org.markeb.eventbus.Event;
import org.markeb.eventbus.EventBusType;
import org.markeb.eventbus.EventHandler;
import org.markeb.eventbus.EventSubscriber;
import org.markeb.eventbus.serialization.EventSerializer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RocketMQ 事件订阅者实现
 */
public class RocketMQEventSubscriber implements EventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(RocketMQEventSubscriber.class);

    private final String nameServerAddr;
    private final String consumerGroup;
    private final EventSerializer eventSerializer;
    private final Map<String, DefaultMQPushConsumer> consumers = new ConcurrentHashMap<>();
    private final Map<String, EventHandler<?>> handlers = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public RocketMQEventSubscriber(String nameServerAddr, String consumerGroup,
                                    EventSerializer eventSerializer) {
        this.nameServerAddr = nameServerAddr;
        this.consumerGroup = consumerGroup;
        this.eventSerializer = eventSerializer;
    }

    @Override
    public EventBusType getType() {
        return EventBusType.ROCKETMQ;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void subscribe(String topic, EventHandler<?> handler) {
        handlers.put(topic, handler);

        try {
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup + "_" + topic);
            consumer.setNamesrvAddr(nameServerAddr);
            consumer.subscribe(topic, "*");

            consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                for (MessageExt msg : msgs) {
                    try {
                        Event event = eventSerializer.deserialize(msg.getBody());
                        ((EventHandler<Event>) handler).handle(event);
                    } catch (Exception e) {
                        log.error("Failed to handle RocketMQ event from topic: {}", topic, e);
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });

            consumers.put(topic, consumer);

            if (running.get()) {
                consumer.start();
            }

            log.info("Subscribed to RocketMQ topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to subscribe to RocketMQ topic: {}", topic, e);
            throw new RuntimeException("Failed to subscribe to RocketMQ topic", e);
        }
    }

    @Override
    public void unsubscribe(String topic) {
        DefaultMQPushConsumer consumer = consumers.remove(topic);
        if (consumer != null) {
            consumer.shutdown();
            handlers.remove(topic);
            log.info("Unsubscribed from RocketMQ topic: {}", topic);
        }
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            consumers.forEach((topic, consumer) -> {
                try {
                    consumer.start();
                } catch (Exception e) {
                    log.error("Failed to start RocketMQ consumer for topic: {}", topic, e);
                }
            });
            log.info("RocketMQ event subscriber started");
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            consumers.values().forEach(DefaultMQPushConsumer::shutdown);
            log.info("RocketMQ event subscriber stopped");
        }
    }
}

