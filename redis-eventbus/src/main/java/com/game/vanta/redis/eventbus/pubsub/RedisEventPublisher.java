package com.game.vanta.redis.eventbus.pubsub;

import com.game.vanta.redis.eventbus.RedisEvent;
import com.game.vanta.redis.eventbus.RedisEventProtostuffSchemaPool;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisEventPublisher {

    private final RedisTemplate<String, RedisEventWrapper> eventRedisTemplate;

    private final RedisEventProtostuffSchemaPool schemaPool;

    public RedisEventPublisher(
        RedisTemplate<String, RedisEventWrapper> eventRedisTemplate,
        RedisEventProtostuffSchemaPool schemaPool) {
        this.eventRedisTemplate = eventRedisTemplate;
        this.schemaPool = schemaPool;
    }

    public void publish(RedisEvent event) {
        RedisEventWrapper redisEventWrapper = new RedisEventWrapper();
        redisEventWrapper.setClassName(event.getClass().getName());
        redisEventWrapper.setBody(schemaPool.serialize(event));
        eventRedisTemplate.convertAndSend(event.channel(), redisEventWrapper);
    }

}
