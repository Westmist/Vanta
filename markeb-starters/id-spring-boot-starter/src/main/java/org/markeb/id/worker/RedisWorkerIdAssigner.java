package org.markeb.id.worker;

import org.markeb.id.config.IdGeneratorProperties;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的 WorkerId 自动分配器
 * 
 * 工作原理：
 * 1. 遍历 [minWorkerId, maxWorkerId] 范围
 * 2. 使用 Redis SETNX 尝试获取锁
 * 3. 成功获取后定期续租
 * 4. 服务停止时释放 WorkerId
 */
public class RedisWorkerIdAssigner implements WorkerIdAssigner {

    private static final Logger log = LoggerFactory.getLogger(RedisWorkerIdAssigner.class);

    private final StringRedisTemplate redisTemplate;
    private final IdGeneratorProperties properties;
    private final String instanceId;
    private final ScheduledExecutorService scheduler;

    private volatile int assignedWorkerId = -1;
    private volatile boolean running = false;

    public RedisWorkerIdAssigner(StringRedisTemplate redisTemplate, IdGeneratorProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.instanceId = generateInstanceId();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "worker-id-renew");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public int assignWorkerId() {
        if (assignedWorkerId >= 0) {
            return assignedWorkerId;
        }

        IdGeneratorProperties.AutoRegisterConfig config = properties.getAutoRegisterConfig();
        int minId = config.getMinWorkerId();
        int maxId = config.getMaxWorkerId();
        
        // 如果未配置最大值，根据位长度计算
        if (maxId < 0) {
            maxId = (1 << properties.getWorkerIdBitLength()) - 1;
        }

        String keyPrefix = config.getKeyPrefix();
        int leaseSeconds = config.getLeaseSeconds();

        // 遍历尝试获取 WorkerId
        for (int workerId = minId; workerId <= maxId; workerId++) {
            String key = keyPrefix + workerId;
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(key, instanceId, leaseSeconds, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(success)) {
                assignedWorkerId = workerId;
                running = true;
                log.info("Successfully assigned WorkerId: {}, instanceId: {}", workerId, instanceId);
                
                // 启动续租任务
                startRenewTask(key, leaseSeconds, config.getRenewIntervalSeconds());
                return workerId;
            }
        }

        throw new IllegalStateException("Failed to assign WorkerId, all IDs in range [" + minId + ", " + maxId + "] are occupied");
    }

    /**
     * 启动续租任务
     */
    private void startRenewTask(String key, int leaseSeconds, int renewIntervalSeconds) {
        scheduler.scheduleAtFixedRate(() -> {
            if (!running) {
                return;
            }
            try {
                // 检查当前持有者是否是自己
                String currentHolder = redisTemplate.opsForValue().get(key);
                if (instanceId.equals(currentHolder)) {
                    // 续租
                    redisTemplate.expire(key, leaseSeconds, TimeUnit.SECONDS);
                    log.debug("Renewed WorkerId {} lease", assignedWorkerId);
                } else {
                    log.warn("WorkerId {} was taken by another instance: {}", assignedWorkerId, currentHolder);
                    running = false;
                }
            } catch (Exception e) {
                log.error("Failed to renew WorkerId {} lease", assignedWorkerId, e);
            }
        }, renewIntervalSeconds, renewIntervalSeconds, TimeUnit.SECONDS);
    }

    /**
     * 释放 WorkerId
     */
    @PreDestroy
    public void release() {
        running = false;
        scheduler.shutdown();

        if (assignedWorkerId >= 0) {
            String key = properties.getAutoRegisterConfig().getKeyPrefix() + assignedWorkerId;
            try {
                // 只有当前持有者是自己时才删除
                String currentHolder = redisTemplate.opsForValue().get(key);
                if (instanceId.equals(currentHolder)) {
                    redisTemplate.delete(key);
                    log.info("Released WorkerId: {}", assignedWorkerId);
                }
            } catch (Exception e) {
                log.error("Failed to release WorkerId: {}", assignedWorkerId, e);
            }
        }
    }

    /**
     * 生成实例唯一标识
     */
    private String generateInstanceId() {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            hostname = "unknown";
        }
        return hostname + "-" + ProcessHandle.current().pid() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 获取已分配的 WorkerId
     */
    public int getAssignedWorkerId() {
        return assignedWorkerId;
    }

    /**
     * 是否正在运行
     */
    public boolean isRunning() {
        return running;
    }
}

