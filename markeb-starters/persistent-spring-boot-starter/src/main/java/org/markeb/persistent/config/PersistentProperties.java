package org.markeb.persistent.config;

import org.markeb.persistent.cache.CacheType;
import org.markeb.persistent.queue.QueueType;
import org.markeb.persistent.repository.RepositoryType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 持久化配置属性
 */
@Data
@ConfigurationProperties(prefix = "markeb.persistent")
public class PersistentProperties {

    /**
     * 是否启用持久化模块
     */
    private boolean enabled = true;

    /**
     * 存储配置
     */
    private StorageConfig storage = new StorageConfig();

    /**
     * 缓存配置
     */
    private CacheConfig cache = new CacheConfig();

    /**
     * 队列配置
     */
    private QueueConfig queue = new QueueConfig();

    @Data
    public static class StorageConfig {
        /**
         * 存储类型
         */
        private RepositoryType type = RepositoryType.MONGO;
    }

    @Data
    public static class CacheConfig {
        /**
         * 缓存类型
         */
        private CacheType type = CacheType.REDIS;

        /**
         * Redis 配置
         */
        private RedisConfig redis = new RedisConfig();

        /**
         * Caffeine 配置
         */
        private CaffeineConfig caffeine = new CaffeineConfig();
    }

    @Data
    public static class RedisConfig {
        /**
         * 键前缀
         */
        private String keyPrefix = "persistent:";

        /**
         * 默认 TTL
         */
        private Duration defaultTtl = Duration.ofMinutes(30);
    }

    @Data
    public static class CaffeineConfig {
        /**
         * 最大缓存数量
         */
        private long maxSize = 10000;

        /**
         * 写入后过期时间
         */
        private Duration expireAfterWrite = Duration.ofMinutes(5);
    }

    @Data
    public static class QueueConfig {
        /**
         * 队列类型
         */
        private QueueType type = QueueType.MEMORY;

        /**
         * 主题名称
         */
        private String topic = "persistent-topic";

        /**
         * 是否启用异步持久化
         */
        private boolean asyncEnabled = true;

        /**
         * RocketMQ 配置
         */
        private RocketMQConfig rocketmq = new RocketMQConfig();

        /**
         * Kafka 配置
         */
        private KafkaConfig kafka = new KafkaConfig();

        /**
         * 内存队列配置
         */
        private MemoryConfig memory = new MemoryConfig();
    }

    @Data
    public static class RocketMQConfig {
        /**
         * 消费者组
         */
        private String consumerGroup = "persistent-consumer";
    }

    @Data
    public static class KafkaConfig {
        /**
         * 消费者组
         */
        private String groupId = "persistent-consumer";
    }

    @Data
    public static class MemoryConfig {
        /**
         * 队列容量
         */
        private int capacity = 10000;
    }
}
