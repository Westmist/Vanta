package org.markeb.locate.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.markeb.locate.LocateService;
import org.markeb.locate.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 基于 Redis 的玩家定位服务实现
 */
public class RedisLocateService implements LocateService {

    private static final Logger log = LoggerFactory.getLogger(RedisLocateService.class);

    private static final String LOCATION_PREFIX = "markeb:locate:player:";
    private static final String NODE_PLAYERS_PREFIX = "markeb:locate:node:";
    private static final String ONLINE_COUNT_KEY = "markeb:locate:online_count";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration expireTime;

    public RedisLocateService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, Duration expireTime) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.expireTime = expireTime;
    }

    @Override
    public void bind(String playerId, Location location) {
        try {
            location.setLastUpdateTime(LocalDateTime.now());
            String json = objectMapper.writeValueAsString(location);
            String key = LOCATION_PREFIX + playerId;

            // 存储位置信息
            redisTemplate.opsForValue().set(key, json, expireTime);

            // 添加到节点玩家集合
            String nodeKey = NODE_PLAYERS_PREFIX + location.getNodeId();
            redisTemplate.opsForSet().add(nodeKey, playerId);

            // 增加在线计数
            redisTemplate.opsForValue().increment(ONLINE_COUNT_KEY);

            log.debug("Bound player {} to node {}", playerId, location.getNodeId());
        } catch (Exception e) {
            log.error("Failed to bind player location: {}", playerId, e);
        }
    }

    @Override
    public void unbind(String playerId) {
        try {
            String key = LOCATION_PREFIX + playerId;
            String json = redisTemplate.opsForValue().get(key);

            if (json != null) {
                Location location = objectMapper.readValue(json, Location.class);

                // 从节点玩家集合移除
                String nodeKey = NODE_PLAYERS_PREFIX + location.getNodeId();
                redisTemplate.opsForSet().remove(nodeKey, playerId);

                // 减少在线计数
                redisTemplate.opsForValue().decrement(ONLINE_COUNT_KEY);
            }

            // 删除位置信息
            redisTemplate.delete(key);

            log.debug("Unbound player {}", playerId);
        } catch (Exception e) {
            log.error("Failed to unbind player location: {}", playerId, e);
        }
    }

    @Override
    public Optional<Location> locate(String playerId) {
        try {
            String key = LOCATION_PREFIX + playerId;
            String json = redisTemplate.opsForValue().get(key);

            if (json != null) {
                Location location = objectMapper.readValue(json, Location.class);
                return Optional.of(location);
            }
        } catch (Exception e) {
            log.error("Failed to locate player: {}", playerId, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Location> locateAll(List<String> playerIds) {
        List<Location> locations = new ArrayList<>();
        for (String playerId : playerIds) {
            locate(playerId).ifPresent(locations::add);
        }
        return locations;
    }

    @Override
    public boolean isOnline(String playerId) {
        String key = LOCATION_PREFIX + playerId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public List<String> getPlayersByNode(String nodeId) {
        String nodeKey = NODE_PLAYERS_PREFIX + nodeId;
        Set<String> members = redisTemplate.opsForSet().members(nodeKey);
        return members != null ? new ArrayList<>(members) : new ArrayList<>();
    }

    @Override
    public long getOnlineCount() {
        String count = redisTemplate.opsForValue().get(ONLINE_COUNT_KEY);
        return count != null ? Long.parseLong(count) : 0;
    }

    @Override
    public void refresh(String playerId) {
        String key = LOCATION_PREFIX + playerId;
        redisTemplate.expire(key, expireTime);
    }
}

