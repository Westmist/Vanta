package org.markeb.lock.config;

import org.markeb.lock.DistributedLock;
import org.markeb.lock.LockType;
import org.markeb.lock.aspect.DistributedLockAspect;
import org.markeb.lock.local.LocalDistributedLock;
import org.markeb.lock.redis.RedisDistributedLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分布式锁自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(LockProperties.class)
@ConditionalOnProperty(prefix = "markeb.lock", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LockAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LockAutoConfiguration.class);

    @Configuration
    @ConditionalOnClass(RedissonClient.class)
    @ConditionalOnProperty(prefix = "markeb.lock", name = "type", havingValue = "REDIS", matchIfMissing = true)
    static class RedisLockConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public DistributedLock redisDistributedLock(RedissonClient redissonClient) {
            log.info("Initializing Redis distributed lock");
            return new RedisDistributedLock(redissonClient);
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "markeb.lock", name = "type", havingValue = "LOCAL")
    static class LocalLockConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public DistributedLock localDistributedLock() {
            log.info("Initializing Local distributed lock (for testing only)");
            return new LocalDistributedLock();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public DistributedLockAspect distributedLockAspect(DistributedLock distributedLock) {
        return new DistributedLockAspect(distributedLock);
    }
}

