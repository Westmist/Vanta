package com.game.vanta.persistent.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.vanta.persistent.PersistentPool;
import com.game.vanta.persistent.dao.IPersistent;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = "persistent-topic",
        consumerGroup = "persistent-consumer-group",
        messageModel = MessageModel.CLUSTERING
)
public class PersistentMessageConsumer implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(PersistentMessageConsumer.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final PersistentPool persistentPool;

    private final RedisTemplate<String, IPersistent> redisTemplate;

    private final MongoTemplate mongoTemplate;

    public PersistentMessageConsumer(
            PersistentPool persistentPool,
            RedisTemplate<String, IPersistent> redisTemplate,
            MongoTemplate mongoTemplate) {
        this.persistentPool = persistentPool;
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void onMessage(String message) {
        log.info("Received persistent message: {}", message);
        PersistentMqPost mqPost;
        // 1. 反序列化 MQ 消息
        try {
            mqPost = objectMapper.readValue(message, PersistentMqPost.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String collectName = mqPost.getCollectName();
        String id = mqPost.getId();

        // 2. 根据 collection 找 class
        Class<? extends IPersistent> clazz = persistentPool.findClazz(collectName);
        if (clazz == null) {
            return;
//            throw new RuntimeException("Cannot find class corresponding to collection: " + collectName);
        }

        // 3. 从 Redis 拉对象
        String key = persistentPool.persistentKey(clazz, id);
        IPersistent data = redisTemplate.opsForValue().get(key);
        if (data == null) {
            log.warn("Cache miss in Redis for key: {}", key);
            return;
        }
        mongoTemplate.save(data);
    }

}
