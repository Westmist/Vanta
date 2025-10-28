package com.game.vanta.persistent.checker;

import com.game.vanta.persistent.checker.abs.IChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;


public class RedisConnectivityChecker implements IChecker {

    private static final Logger log = LoggerFactory.getLogger(RedisConnectivityChecker.class);

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisConnectivityChecker(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public String name() {
        return "Redis";
    }

    @Override
    public void check() {
        String redisInfo = "unknown";
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

        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String pong = connection.ping();
            if (!"PONG".equalsIgnoreCase(pong)) {
                throw new IllegalStateException("Unexpected Redis ping response: " + pong);
            }
            log.info("Redis ping response: {}", pong);
        }
    }


}
