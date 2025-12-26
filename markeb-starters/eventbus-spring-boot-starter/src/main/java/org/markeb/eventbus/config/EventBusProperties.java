package org.markeb.eventbus.config;

import org.markeb.eventbus.EventBusType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件总线配置属性
 */
@Data
@ConfigurationProperties(prefix = "markeb.eventbus")
public class EventBusProperties {

    /**
     * 是否启用事件总线
     */
    private boolean enabled = true;

    /**
     * 事件总线类型
     */
    private EventBusType type = EventBusType.REDIS;

    /**
     * 事件处理器扫描包路径
     */
    private List<String> handlerPackages = new ArrayList<>();

    /**
     * Redis 配置
     */
    private RedisConfig redis = new RedisConfig();

    /**
     * Kafka 配置
     */
    private KafkaConfig kafka = new KafkaConfig();

    /**
     * RocketMQ 配置
     */
    private RocketMQConfig rocketmq = new RocketMQConfig();

    @Data
    public static class RedisConfig {
        /**
         * 订阅的主题列表
         */
        private List<String> topics = new ArrayList<>();
    }

    @Data
    public static class KafkaConfig {
        /**
         * 消费者组ID
         */
        private String groupId = "markeb-eventbus";

        /**
         * 订阅的主题列表
         */
        private List<String> topics = new ArrayList<>();

        /**
         * 并发消费者数量
         */
        private int concurrency = 1;
    }

    @Data
    public static class RocketMQConfig {
        /**
         * NameServer 地址
         */
        private String nameServer = "localhost:9876";

        /**
         * 消费者组
         */
        private String consumerGroup = "markeb-eventbus";

        /**
         * 生产者组
         */
        private String producerGroup = "markeb-eventbus-producer";

        /**
         * 订阅的主题列表
         */
        private List<String> topics = new ArrayList<>();
    }
}

