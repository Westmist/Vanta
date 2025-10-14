package com.game.vanta.persistent;

import com.game.vanta.persistent.dao.IPersistent;
import com.game.vanta.persistent.producer.DefaultPersistentMqSendCallback;
import com.game.vanta.persistent.producer.PersistentMessageProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class PersistentConfiguration {

  @Bean
  public PersistentTemplate persistentTemplate(
      RedisTemplate<String, IPersistent> redisTemplate,
      MongoTemplate mongoTemplate,
      PersistentPool persistentPool,
      PersistentMessageProducer persistentMessageProducer) {
    return new PersistentTemplate(
        redisTemplate, mongoTemplate, persistentPool, persistentMessageProducer);
  }

  @Bean
  @ConditionalOnMissingBean(SendCallback.class)
  public SendCallback persistentMqSendCallback() {
    return new DefaultPersistentMqSendCallback();
  }
}
