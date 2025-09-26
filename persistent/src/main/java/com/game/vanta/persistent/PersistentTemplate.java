package com.game.vanta.persistent;

import com.game.vanta.persistent.dao.IPersistent;
import com.game.vanta.persistent.mq.PersistentMessageConsumer;
import com.game.vanta.persistent.mq.PersistentMessageProducer;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;


public class PersistentTemplate implements IPersistentService {

    private final RedisTemplate<String, IPersistent> redisTemplate;

    private final MongoTemplate mongoTemplate;

    private final PersistentPool persistentPool;

    private final PersistentMessageProducer persistentMessageProducer;

    @SuppressWarnings("unchecked")
    public PersistentTemplate(
            RedisTemplate<String, IPersistent> redisTemplate,
            MongoTemplate mongoTemplate,
            PersistentPool persistentPool, PersistentMessageProducer persistentMessageProducer) {
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
        this.persistentPool = persistentPool;
        this.persistentMessageProducer = persistentMessageProducer;
    }

    @Override
    public <T extends IPersistent> T find(Class<T> clazz, String id) {
        String key = persistentPool.persistentKey(clazz, id);
        // 先查 Redis
        IPersistent cache = redisTemplate.opsForValue().get(key);
        if (cache != null) {
            return cast(cache, clazz);
        }
        // 再查 Mongo
        T data = mongoTemplate.findById(id, clazz);
        if (data != null) {
            // 回写 Redis
            redisTemplate.opsForValue().set(key, data, data.timeout());
        }
        return data;
    }

    @Override
    public <T extends IPersistent> T updateAsync(T data) {
        String key = persistentPool.persistentKey(data.getClass(), data.getId());
        redisTemplate.opsForValue().set(key, data, data.timeout());
        persistentMessageProducer.sendMqPost(data);
        return data;
    }

    @Override
    public <T extends IPersistent> T saveNow(T data) {
        return null;
    }

    @Override
    public <T extends IPersistent> void remove(T data) {

    }

    private <T extends IPersistent> T cast(Object obj, Class<T> clazz) {
        return clazz.cast(obj);
    }


}
