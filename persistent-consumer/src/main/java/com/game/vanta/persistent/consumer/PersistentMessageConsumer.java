package com.game.vanta.persistent.consumer;

import com.game.vanta.persistent.PersistentPool;
import com.game.vanta.persistent.dao.IPersistent;
import com.game.vanta.persistent.mq.PersistentMqNotice;
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
        topic = "${persistent.topic:persistent-topic}",
        consumerGroup = "${rocketmq.consumer.group}",
        messageModel = MessageModel.CLUSTERING
)
public class PersistentMessageConsumer implements RocketMQListener<PersistentMqNotice> {

    private static final Logger log = LoggerFactory.getLogger(PersistentMessageConsumer.class);

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
    public void onMessage(PersistentMqNotice mqNotice) {
        log.info("Received persistent message: {}", mqNotice);
        String collectName = mqNotice.getCollectName();
        String id = mqNotice.getId();
        Class<? extends IPersistent> clazz = persistentPool.findClazz(collectName);
        if (clazz == null) {
            log.error("Unknown collect name: {}", collectName);
            return;
        }
        String key = persistentPool.persistentKey(clazz, id);
        IPersistent data = redisTemplate.opsForValue().get(key);
        if (data == null) {
            log.error("Cache miss in Redis for key: {}", key);
            return;
        }
        mongoTemplate.save(data);
    }

}
