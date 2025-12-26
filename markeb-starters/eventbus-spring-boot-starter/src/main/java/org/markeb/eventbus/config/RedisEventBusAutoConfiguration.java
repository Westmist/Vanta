package org.markeb.eventbus.config;

import org.markeb.eventbus.EventPublisher;
import org.markeb.eventbus.EventSubscriber;
import org.markeb.eventbus.redis.RedisEventPublisher;
import org.markeb.eventbus.redis.RedisEventSubscriber;
import org.markeb.eventbus.serialization.EventSerializer;
import org.markeb.eventbus.serialization.ProtostuffEventSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis 事件总线自动配置
 */
@Configuration
@ConditionalOnClass(RedisTemplate.class)
@ConditionalOnProperty(prefix = "markeb.eventbus", name = "type", havingValue = "redis", matchIfMissing = true)
@EnableConfigurationProperties(EventBusProperties.class)
public class RedisEventBusAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RedisEventBusAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public EventSerializer eventSerializer() {
        return new ProtostuffEventSerializer();
    }

    @Bean("eventBusRedisTemplate")
    @ConditionalOnMissingBean(name = "eventBusRedisTemplate")
    public RedisTemplate<String, byte[]> eventBusRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.byteArray());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.byteArray());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher redisEventPublisher(RedisTemplate<String, byte[]> eventBusRedisTemplate,
                                               EventSerializer eventSerializer) {
        log.info("Creating Redis EventPublisher");
        return new RedisEventPublisher(eventBusRedisTemplate, eventSerializer);
    }

    @Bean
    @ConditionalOnMissingBean(EventSubscriber.class)
    public EventSubscriber redisEventSubscriber(RedisMessageListenerContainer listenerContainer,
                                                 EventSerializer eventSerializer) {
        log.info("Creating Redis EventSubscriber");
        return new RedisEventSubscriber(listenerContainer, eventSerializer);
    }
}

