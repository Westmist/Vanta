package com.game.vanta.persistent;

import com.game.vanta.persistent.config.PersistentProperties;
import com.game.vanta.persistent.dao.IPersistent;
import com.game.vanta.persistent.producer.DefaultPersistentMqSendCallback;
import com.game.vanta.persistent.producer.PersistentMessageProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class PersistentAutoConfiguration {

    @Bean
    public PersistentMessageProducer persistentMessageProducer(
        PersistentPool persistentPool,
        RocketMQTemplate rocketMQTemplate,
        PersistentProperties persistentProperties,
        SendCallback persistentMqSendCallback) {
        return new PersistentMessageProducer(persistentPool, rocketMQTemplate, persistentProperties, persistentMqSendCallback);
    }

    @Bean
    public PersistentTemplate persistentTemplate(
        RedisTemplate<String, IPersistent> redisTemplate,
        MongoTemplate mongoTemplate,
        PersistentPool persistentPool,
        PersistentMessageProducer persistentMessageProducer) {
        return new PersistentTemplate(redisTemplate, mongoTemplate, persistentPool, persistentMessageProducer);
    }

    @Bean
    @ConditionalOnBean(IPersistentService.class)
    public DataCenter dataCenter(IPersistentService service) {
        return new DataCenter(service);
    }

    @Bean
    @ConditionalOnMissingBean(SendCallback.class)
    public SendCallback persistentMqSendCallback() {
        return new DefaultPersistentMqSendCallback();
    }

}
