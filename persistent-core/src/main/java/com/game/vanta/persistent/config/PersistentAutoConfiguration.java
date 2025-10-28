package com.game.vanta.persistent.config;

import com.game.vanta.persistent.PersistentPool;
import com.game.vanta.persistent.dao.IPersistent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;


@Configuration
@EnableConfigurationProperties({PersistentProperties.class})
public class PersistentAutoConfiguration {

    @Bean("persistentRedisTemplate")
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
    public PersistentPool persistentPool(PersistentProperties persistentProperties, MongoTemplate mongoTemplate) {
        return new PersistentPool(persistentProperties, mongoTemplate);
    }

}
