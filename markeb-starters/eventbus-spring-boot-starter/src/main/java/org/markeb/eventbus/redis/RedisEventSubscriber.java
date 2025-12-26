package org.markeb.eventbus.redis;

import org.markeb.eventbus.Event;
import org.markeb.eventbus.EventBusType;
import org.markeb.eventbus.EventHandler;
import org.markeb.eventbus.EventSubscriber;
import org.markeb.eventbus.serialization.EventSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis 事件订阅者实现
 */
public class RedisEventSubscriber implements EventSubscriber {

    private static final Logger log = LoggerFactory.getLogger(RedisEventSubscriber.class);

    private final RedisMessageListenerContainer listenerContainer;
    private final EventSerializer eventSerializer;
    private final Map<String, MessageListener> listeners = new ConcurrentHashMap<>();
    private final Map<String, EventHandler<?>> handlers = new ConcurrentHashMap<>();

    public RedisEventSubscriber(RedisMessageListenerContainer listenerContainer,
                                 EventSerializer eventSerializer) {
        this.listenerContainer = listenerContainer;
        this.eventSerializer = eventSerializer;
    }

    @Override
    public EventBusType getType() {
        return EventBusType.REDIS;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void subscribe(String topic, EventHandler<?> handler) {
        handlers.put(topic, handler);

        MessageListener listener = (message, pattern) -> {
            try {
                Event event = eventSerializer.deserialize(message.getBody());
                ((EventHandler<Event>) handler).handle(event);
            } catch (Exception e) {
                log.error("Failed to handle event from topic: {}", topic, e);
            }
        };

        listeners.put(topic, listener);
        listenerContainer.addMessageListener(listener, new ChannelTopic(topic));
        log.info("Subscribed to Redis topic: {}", topic);
    }

    @Override
    public void unsubscribe(String topic) {
        MessageListener listener = listeners.remove(topic);
        if (listener != null) {
            listenerContainer.removeMessageListener(listener, new ChannelTopic(topic));
            handlers.remove(topic);
            log.info("Unsubscribed from Redis topic: {}", topic);
        }
    }

    @Override
    public void start() {
        if (!listenerContainer.isRunning()) {
            listenerContainer.start();
            log.info("Redis event subscriber started");
        }
    }

    @Override
    public void stop() {
        if (listenerContainer.isRunning()) {
            listenerContainer.stop();
            log.info("Redis event subscriber stopped");
        }
    }
}

