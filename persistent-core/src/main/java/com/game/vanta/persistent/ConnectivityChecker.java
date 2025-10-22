package com.game.vanta.persistent;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 检测存储依赖是否可用
 */
@Component
public class ConnectivityChecker {

    private static final Logger log = LoggerFactory.getLogger(ConnectivityChecker.class);

    private final MongoClient mongoClient;

    private final RedisConnectionFactory redisConnectionFactory;

    private final MongoProperties mongoProperties;

    private final DefaultMQProducer rocketMQProducer;

    private final List<DefaultRocketMQListenerContainer> listenerContainers;

    private final String healthCheckTopic = "health-check-topic";

    public ConnectivityChecker(MongoClient mongoClient,
                               RedisConnectionFactory redisConnectionFactory,
                               MongoProperties mongoProperties,
                               @Autowired(required = false) DefaultMQProducer rocketMQProducer,
                               @Autowired(required = false) List<DefaultRocketMQListenerContainer> listenerContainers) {
        this.mongoClient = mongoClient;
        this.redisConnectionFactory = redisConnectionFactory;
        this.mongoProperties = mongoProperties;
        this.rocketMQProducer = rocketMQProducer;
        this.listenerContainers = listenerContainers;
    }

    @PostConstruct
    public void checkDependencies() {
        checkRedis();
        checkRocketMQ();
        checkMongo();
    }

    /**
     * 检查 Redis 可用性，兼容任意 RedisConnectionFactory
     */
    private void checkRedis() {
        String redisInfo = "unknown";

        try {
            if (redisConnectionFactory instanceof LettuceConnectionFactory lettuce) {
                if (lettuce.getClusterConfiguration() != null) {
                    // Cluster 模式
                    redisInfo = "Cluster nodes: " + lettuce.getClusterConfiguration().getClusterNodes();
                } else if (lettuce.getSentinelConfiguration() != null) {
                    // Sentinel 模式
                    redisInfo = "Sentinel master: " + lettuce.getSentinelConfiguration().getMaster() +
                        ", nodes: " + lettuce.getSentinelConfiguration().getSentinels();
                } else {
                    // Standalone 模式
                    var conf = lettuce.getStandaloneConfiguration();
                    redisInfo = String.format("SingleNode %s:%d/%d",
                        conf.getHostName(), conf.getPort(), conf.getDatabase());
                }
            } else if (redisConnectionFactory instanceof JedisConnectionFactory jedis) {
                if (jedis.getClusterConfiguration() != null) {
                    redisInfo = "Cluster nodes: " + jedis.getClusterConfiguration().getClusterNodes();
                } else if (jedis.getSentinelConfiguration() != null) {
                    redisInfo = "Sentinel master: " + jedis.getSentinelConfiguration().getMaster() +
                        ", nodes: " + jedis.getSentinelConfiguration().getSentinels();
                } else {
                    var conf = jedis.getStandaloneConfiguration();
                    redisInfo = String.format("SingleNode %s:%d/%d",
                        conf.getHostName(), conf.getPort(), conf.getDatabase());
                }
            }

            log.info("Checking Redis: {}", redisInfo);

            try (var connection = redisConnectionFactory.getConnection()) {
                String pong = connection.ping();
                if (!"PONG".equalsIgnoreCase(pong)) {
                    throw new IllegalStateException("Unexpected Redis ping response: " + pong);
                }
                log.info("Redis ping response: {}", pong);
            }
        } catch (Exception e) {
            throw logAndWrap("Redis", e);
        }
    }

    /**
     * 检查 RocketMQ 可用性
     */
    private void checkRocketMQ() {

        // 检查生产者
        if (rocketMQProducer != null) {
            try {
                String ns = rocketMQProducer.getNamesrvAddr();
                log.info("Checking RocketMQ producer, NameServer={}", ns);

                // 发送轻量测试消息
                SendResult sendResult = rocketMQProducer.send(new Message(
                    healthCheckTopic, "ping".getBytes()
                ));
                log.info("RocketMQ test message sent: {}", sendResult);
            } catch (Exception e) {
                throw new IllegalStateException("RocketMQ producer check failed", e);
            }
        }

        // 检查消费者
        if (listenerContainers != null) {
            for (DefaultRocketMQListenerContainer container : listenerContainers) {
                try {
                    log.info("Checking consumer, group={}, topic={}",
                        container.getConsumerGroup(),
                        container.getTopic());

                    // 尝试获取订阅队列，如果能成功说明能连上 Broker
                    container.getConsumer().fetchSubscribeMessageQueues(container.getTopic());

                    log.info("RocketMQ consumer check OK, group={}, topic={}",
                        container.getConsumerGroup(), container.getTopic());

                } catch (Exception e) {
                    throw logAndWrap("RocketMQ consumer " + container.getConsumerGroup(), e);
                }
            }
        }

    }

    /**
     * 检查 MongoDB 可用性
     */
    private void checkMongo() {
        String mongoUri = mongoProperties.getUri();
        String dbName = mongoProperties.getMongoClientDatabase();
        log.info("Checking MongoDB: {}, database: {}", mongoUri, dbName);

        try {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            Document result = db.runCommand(new Document("ping", 1));
            Object ok = result.get("ok");
            if (!(ok instanceof Number) || ((Number) ok).intValue() != 1) {
                throw new IllegalStateException("Unexpected MongoDB ping response: " + result.toJson());
            }
            log.info("MongoDB ping response: {}", result.toJson());
        } catch (Exception e) {
            throw logAndWrap("MongoDB", e);
        }
    }

    /**
     * 统一异常日志与包装
     */
    private IllegalStateException logAndWrap(String name, Exception e) {
        log.error("{} is not available: {}", name, e.getMessage(), e);
        return new IllegalStateException(name + " is not available", e);
    }

}
