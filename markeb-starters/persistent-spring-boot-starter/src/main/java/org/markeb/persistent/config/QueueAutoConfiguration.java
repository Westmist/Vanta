package org.markeb.persistent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.markeb.persistent.queue.NonePersistentQueue;
import org.markeb.persistent.queue.PersistentQueue;
import org.markeb.persistent.queue.kafka.KafkaPersistentQueue;
import org.markeb.persistent.queue.memory.MemoryPersistentQueue;
import org.markeb.persistent.queue.rocketmq.RocketMQPersistentQueue;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * 队列层自动配置
 */
@Configuration
public class QueueAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(QueueAutoConfiguration.class);

    /**
     * RocketMQ 队列配置
     */
    @Configuration
    @ConditionalOnClass(RocketMQTemplate.class)
    @ConditionalOnProperty(prefix = "markeb.persistent.queue", name = "type", havingValue = "rocketmq")
    public static class RocketMQQueueConfiguration {

        @Bean
        @ConditionalOnMissingBean(PersistentQueue.class)
        public PersistentQueue rocketMQPersistentQueue(RocketMQTemplate rocketMQTemplate,
                                                        PersistentProperties properties) {
            String topic = properties.getQueue().getTopic();
            log.info("Creating RocketMQ PersistentQueue with topic: {}", topic);
            return new RocketMQPersistentQueue(rocketMQTemplate, topic);
        }
    }

    /**
     * Kafka 队列配置
     */
    @Configuration
    @ConditionalOnClass(KafkaTemplate.class)
    @ConditionalOnProperty(prefix = "markeb.persistent.queue", name = "type", havingValue = "kafka")
    public static class KafkaQueueConfiguration {

        @Bean
        @ConditionalOnMissingBean(PersistentQueue.class)
        public PersistentQueue kafkaPersistentQueue(KafkaTemplate<String, byte[]> kafkaTemplate,
                                                     PersistentProperties properties,
                                                     ObjectMapper objectMapper) {
            String topic = properties.getQueue().getTopic();
            log.info("Creating Kafka PersistentQueue with topic: {}", topic);
            return new KafkaPersistentQueue(kafkaTemplate, topic, objectMapper);
        }
    }

    /**
     * 内存队列配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "markeb.persistent.queue", name = "type", havingValue = "memory", matchIfMissing = true)
    public static class MemoryQueueConfiguration {

        @Bean
        @ConditionalOnMissingBean(PersistentQueue.class)
        public PersistentQueue memoryPersistentQueue(PersistentProperties properties) {
            int capacity = properties.getQueue().getMemory().getCapacity();
            log.info("Creating Memory PersistentQueue with capacity: {}", capacity);
            return new MemoryPersistentQueue(capacity);
        }
    }

    /**
     * 无队列配置（同步模式）
     */
    @Configuration
    @ConditionalOnProperty(prefix = "markeb.persistent.queue", name = "type", havingValue = "none")
    public static class NoneQueueConfiguration {

        @Bean
        @ConditionalOnMissingBean(PersistentQueue.class)
        public PersistentQueue nonePersistentQueue() {
            log.info("Creating None PersistentQueue (sync mode)");
            return new NonePersistentQueue();
        }
    }
}

