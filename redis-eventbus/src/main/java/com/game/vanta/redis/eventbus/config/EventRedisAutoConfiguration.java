package com.game.vanta.redis.eventbus.config;

import com.game.vanta.redis.eventbus.RedisEventProtostuffSchemaPool;
import com.game.vanta.redis.eventbus.pubsub.RedisEventPublisher;
import com.game.vanta.redis.eventbus.pubsub.RedisEventSubscriber;
import com.game.vanta.redis.eventbus.pubsub.RedisEventWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class EventRedisAutoConfiguration {

    @Bean("eventRedisTemplate")
    public RedisTemplate<String, RedisEventWrapper> eventRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, RedisEventWrapper> template = new RedisTemplate<>();
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
    @ConditionalOnBean(RedisEventBusConfig.class)
    public RedisEventProtostuffSchemaPool redisEventProtostuffSchemaPool(
        RedisEventBusConfig config,
        ApplicationContext applicationContext) {
        return new RedisEventProtostuffSchemaPool(config.getEventActionPackages(), applicationContext);
    }

    @Bean
    @ConditionalOnBean(RedisEventProtostuffSchemaPool.class)
    public RedisEventPublisher redisEventPublisher(
        RedisTemplate<String, RedisEventWrapper> eventRedisTemplate,
        RedisEventProtostuffSchemaPool schemaPool) {
        return new RedisEventPublisher(eventRedisTemplate, schemaPool);
    }

    @Bean
    @ConditionalOnBean(RedisEventProtostuffSchemaPool.class)
    public RedisEventSubscriber redisEventSubscriber(
        RedisTemplate<String, RedisEventWrapper> eventRedisTemplate,
        RedisEventProtostuffSchemaPool schemaPool) {
        return new RedisEventSubscriber(eventRedisTemplate, schemaPool);
    }

    @Bean
    @ConditionalOnBean(RedisEventBusConfig.class)
    public RedisMessageListenerContainer redisMessageListenerContainer(
        RedisConnectionFactory connectionFactory,
        RedisEventBusConfig config,
        RedisEventSubscriber subscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        for (Topic topic : config.getTopics()) {
            container.addMessageListener(subscriber, topic);
        }
        return container;
    }

}
