package com.game.vanta.redis.eventbus.pubsub;

import com.game.vanta.redis.eventbus.IRedisEventAction;
import com.game.vanta.redis.eventbus.RedisEvent;
import com.game.vanta.redis.eventbus.RedisEventProtostuffSchemaPool;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

public class RedisEventSubscriber implements MessageListener {

    private final RedisTemplate<String, RedisEventWrapper> eventRedisTemplate;

    private final RedisEventProtostuffSchemaPool schemaPool;

    public RedisEventSubscriber(
        RedisTemplate<String, RedisEventWrapper> eventRedisTemplate,
        RedisEventProtostuffSchemaPool schemaPool) {
        this.eventRedisTemplate = eventRedisTemplate;
        this.schemaPool = schemaPool;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        RedisSerializer<?> valueSerializer = eventRedisTemplate.getValueSerializer();
        RedisEventWrapper wrapper = (RedisEventWrapper) valueSerializer.deserialize(message.getBody());
        try {
            // 解析类名，反射生成实例
            Class<?> clazz = Class.forName(wrapper.getClassName());
            RedisEvent event = (RedisEvent) clazz.getDeclaredConstructor().newInstance();
            RedisEvent t = schemaPool.deserialize(wrapper.getBody(), event);
            IRedisEventAction eventAction = schemaPool.findEventAction(t.getClass());
            try {
                eventAction.onEvent(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
