package org.markeb.eventbus.config;

import org.markeb.eventbus.EventPublisher;
import org.markeb.eventbus.EventSubscriber;
import org.markeb.eventbus.kafka.KafkaEventPublisher;
import org.markeb.eventbus.kafka.KafkaEventSubscriber;
import org.markeb.eventbus.serialization.EventSerializer;
import org.markeb.eventbus.serialization.ProtostuffEventSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 事件总线自动配置
 */
@Configuration
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(prefix = "markeb.eventbus", name = "type", havingValue = "kafka")
@EnableConfigurationProperties(EventBusProperties.class)
public class KafkaEventBusAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventBusAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public EventSerializer eventSerializer() {
        return new ProtostuffEventSerializer();
    }

    @Bean("eventBusKafkaTemplate")
    @ConditionalOnMissingBean(name = "eventBusKafkaTemplate")
    public KafkaTemplate<String, byte[]> eventBusKafkaTemplate(ProducerFactory<String, byte[]> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProducerFactory<String, byte[]> producerFactory(
            org.springframework.boot.autoconfigure.kafka.KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConsumerFactory<String, byte[]> consumerFactory(
            org.springframework.boot.autoconfigure.kafka.KafkaProperties kafkaProperties,
            EventBusProperties eventBusProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties(null));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, eventBusProperties.getKafka().getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> kafkaListenerContainerFactory(
            ConsumerFactory<String, byte[]> consumerFactory,
            EventBusProperties eventBusProperties) {
        ConcurrentKafkaListenerContainerFactory<String, byte[]> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(eventBusProperties.getKafka().getConcurrency());
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean(EventPublisher.class)
    public EventPublisher kafkaEventPublisher(KafkaTemplate<String, byte[]> eventBusKafkaTemplate,
                                               EventSerializer eventSerializer) {
        log.info("Creating Kafka EventPublisher");
        return new KafkaEventPublisher(eventBusKafkaTemplate, eventSerializer);
    }

    @Bean
    @ConditionalOnMissingBean(EventSubscriber.class)
    public EventSubscriber kafkaEventSubscriber(
            ConcurrentKafkaListenerContainerFactory<String, byte[]> containerFactory,
            EventSerializer eventSerializer) {
        log.info("Creating Kafka EventSubscriber");
        return new KafkaEventSubscriber(containerFactory, eventSerializer);
    }
}

