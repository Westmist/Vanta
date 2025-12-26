package org.markeb.eventbus.config;

import org.markeb.eventbus.EventPublisher;
import org.markeb.eventbus.EventSubscriber;
import org.markeb.eventbus.rocketmq.RocketMQEventPublisher;
import org.markeb.eventbus.rocketmq.RocketMQEventSubscriber;
import org.markeb.eventbus.serialization.EventSerializer;
import org.markeb.eventbus.serialization.ProtostuffEventSerializer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMQ 事件总线自动配置
 */
@Configuration
@ConditionalOnClass(RocketMQTemplate.class)
@ConditionalOnProperty(prefix = "markeb.eventbus", name = "type", havingValue = "rocketmq")
@EnableConfigurationProperties(EventBusProperties.class)
public class RocketMQEventBusAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RocketMQEventBusAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public EventSerializer eventSerializer() {
        return new ProtostuffEventSerializer();
    }

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher rocketMQEventPublisher(RocketMQTemplate rocketMQTemplate,
                                                  EventSerializer eventSerializer) {
        log.info("Creating RocketMQ EventPublisher");
        return new RocketMQEventPublisher(rocketMQTemplate, eventSerializer);
    }

    @Bean
    @ConditionalOnMissingBean(EventSubscriber.class)
    public EventSubscriber rocketMQEventSubscriber(EventBusProperties properties,
                                                    EventSerializer eventSerializer) {
        EventBusProperties.RocketMQConfig config = properties.getRocketmq();
        log.info("Creating RocketMQ EventSubscriber with nameServer: {}", config.getNameServer());
        return new RocketMQEventSubscriber(config.getNameServer(), config.getConsumerGroup(), eventSerializer);
    }
}

