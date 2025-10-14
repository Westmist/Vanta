package com.game.vanta.persistent;

import com.game.vanta.persistent.dao.IPersistent;
import com.game.vanta.persistent.producer.PersistentMessageProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;

public class PersistentTemplate implements IPersistentService {

  private static final Logger log = LoggerFactory.getLogger(PersistentTemplate.class);

  private final RedisTemplate<String, IPersistent> redisTemplate;

  private final MongoTemplate mongoTemplate;

  private final PersistentPool persistentPool;

  private final PersistentMessageProducer persistentMessageProducer;

  public PersistentTemplate(
      RedisTemplate<String, IPersistent> redisTemplate,
      MongoTemplate mongoTemplate,
      PersistentPool persistentPool,
      PersistentMessageProducer persistentMessageProducer) {
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
    // TODO 后续考虑-避免并发读取 mongo 的缓存击穿问题
    // 再查 Mongo
    T data = mongoTemplate.findById(id, clazz);
    if (data != null) {
      // 回写 Redis
      redisTemplate.opsForValue().set(key, data, data.timeout());
    }
    return data;
  }

  @Override
  public <T extends IPersistent> void upsertAsync(T data) {
    String key = persistentPool.persistentKey(data.getClass(), data.getId());
    redisTemplate.opsForValue().set(key, data, data.timeout());
    persistentMessageProducer.asyncSendMqNotice(data);
  }

  @Override
  public <T extends IPersistent> void upsertNow(T data) {
    SendResult sendResult = persistentMessageProducer.syncSendMqNotice(data);
    SendStatus sendStatus = sendResult.getSendStatus();
    if (sendStatus != SendStatus.SEND_OK) {
      log.error("Failed to send persistent message: {}, status: {}", data, sendStatus);
      throw new RuntimeException("Failed to send persistent message: " + data);
    }
  }

  @Override
  public <T extends IPersistent> void remove(T data) {}

  private <T extends IPersistent> T cast(Object obj, Class<T> clazz) {
    return clazz.cast(obj);
  }
}
