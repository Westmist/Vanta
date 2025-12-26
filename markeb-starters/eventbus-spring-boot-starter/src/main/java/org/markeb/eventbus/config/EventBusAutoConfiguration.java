package org.markeb.eventbus.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 事件总线自动配置入口
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "markeb.eventbus", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(EventBusProperties.class)
@Import({
        RedisEventBusAutoConfiguration.class,
        KafkaEventBusAutoConfiguration.class,
        RocketMQEventBusAutoConfiguration.class,
        EventListenerScanner.class
})
public class EventBusAutoConfiguration {
}

