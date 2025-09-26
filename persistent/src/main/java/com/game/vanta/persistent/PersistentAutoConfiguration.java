package com.game.vanta.persistent;

import com.game.vanta.persistent.dao.IPersistent;
import com.game.vanta.persistent.mq.PersistentMessageProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class PersistentAutoConfiguration {

    @Bean
    public RedisTemplate<String, IPersistent> persistentRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, IPersistent> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public PersistentTemplate persistentTemplate(
            RedisTemplate<String, IPersistent> redisTemplate,
            MongoTemplate mongoTemplate,
            PersistentPool persistentPool,
            PersistentMessageProducer persistentMessageProducer) {
        return new PersistentTemplate(redisTemplate, mongoTemplate, persistentPool, persistentMessageProducer);
    }

}
