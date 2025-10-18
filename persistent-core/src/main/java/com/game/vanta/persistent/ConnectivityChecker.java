package com.game.vanta.persistent;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * 检测存储依赖是否可用
 */
@Component
public class ConnectivityChecker {

    private static final Logger log = LoggerFactory.getLogger(ConnectivityChecker.class);

    private final MongoClient mongoClient;

    private final RedisConnectionFactory redisConnectionFactory;

    private final MongoProperties mongoProperties;

    private final RocketMQTemplate rocketMQTemplate;

    private final String testTopic = "system-startup-test";

    public ConnectivityChecker(
        MongoClient mongoClient,
        RedisConnectionFactory redisConnectionFactory,
        MongoProperties mongoProperties,
        RocketMQTemplate rocketMQTemplate) {
        this.mongoClient = mongoClient;
        this.redisConnectionFactory = redisConnectionFactory;
        this.mongoProperties = mongoProperties;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @PostConstruct
    public void checkDependencies() {
        checkRedis();
        checkMQ();
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
    public void checkMQ() {
        try {
            // 打印 MQ 地址和 topic
            String nameServerAddr = rocketMQTemplate.getProducer().getNamesrvAddr();
            log.info("Checking RocketMQ via RocketMQTemplate, NameServer={}, testTopic={}", nameServerAddr, testTopic);

            // 发送一个轻量测试消息
            rocketMQTemplate.syncSend(testTopic, "ping");
            log.info("RocketMQ is available.");
        } catch (Exception e) {
            throw logAndWrap("RocketMQ", e);
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
