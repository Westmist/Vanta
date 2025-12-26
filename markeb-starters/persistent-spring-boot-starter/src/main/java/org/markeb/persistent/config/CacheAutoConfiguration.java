package org.markeb.persistent.config;

import org.markeb.persistent.cache.CacheManager;
import org.markeb.persistent.cache.NoneCacheManager;
import org.markeb.persistent.cache.caffeine.CaffeineCacheManager;
import org.markeb.persistent.cache.redis.RedisCacheManager;
import org.markeb.persistent.cache.twolevel.TwoLevelCacheManager;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * 缓存层自动配置
 */
@Configuration
public class CacheAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CacheAutoConfiguration.class);

    /**
     * Redis 缓存配置
     */
    @Configuration
    @ConditionalOnClass(RedisTemplate.class)
    @ConditionalOnProperty(prefix = "markeb.persistent.cache", name = "type", havingValue = "redis", matchIfMissing = true)
    public static class RedisCacheConfiguration {

        @Bean("persistentRedisTemplate")
        @ConditionalOnMissingBean(name = "persistentRedisTemplate")
        public RedisTemplate<String, Object> persistentRedisTemplate(RedisConnectionFactory connectionFactory) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
            template.setHashKeySerializer(new StringRedisSerializer());
            template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
            template.afterPropertiesSet();
            return template;
        }

        @Bean
        @ConditionalOnMissingBean(CacheManager.class)
        public CacheManager redisCacheManager(RedisTemplate<String, Object> persistentRedisTemplate,
                                               PersistentProperties properties) {
            PersistentProperties.RedisConfig redisConfig = properties.getCache().getRedis();
            log.info("Creating Redis CacheManager with prefix: {}", redisConfig.getKeyPrefix());
            return new RedisCacheManager(persistentRedisTemplate,
                    redisConfig.getKeyPrefix(),
                    redisConfig.getDefaultTtl());
        }
    }

    /**
     * Caffeine 缓存配置
     */
    @Configuration
    @ConditionalOnClass(Cache.class)
    @ConditionalOnProperty(prefix = "markeb.persistent.cache", name = "type", havingValue = "caffeine")
    public static class CaffeineCacheConfiguration {

        @Bean
        @ConditionalOnMissingBean(CacheManager.class)
        public CacheManager caffeineCacheManager(PersistentProperties properties) {
            PersistentProperties.CaffeineConfig caffeineConfig = properties.getCache().getCaffeine();
            log.info("Creating Caffeine CacheManager with maxSize: {}", caffeineConfig.getMaxSize());
            return new CaffeineCacheManager(caffeineConfig.getMaxSize(), caffeineConfig.getExpireAfterWrite());
        }
    }

    /**
     * 两级缓存配置
     */
    @Configuration
    @ConditionalOnClass({RedisTemplate.class, Cache.class})
    @ConditionalOnProperty(prefix = "markeb.persistent.cache", name = "type", havingValue = "two-level")
    public static class TwoLevelCacheConfiguration {

        @Bean("persistentRedisTemplate")
        @ConditionalOnMissingBean(name = "persistentRedisTemplate")
        public RedisTemplate<String, Object> persistentRedisTemplate(RedisConnectionFactory connectionFactory) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
            template.setHashKeySerializer(new StringRedisSerializer());
            template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
            template.afterPropertiesSet();
            return template;
        }

        @Bean
        @ConditionalOnMissingBean(CacheManager.class)
        public CacheManager twoLevelCacheManager(RedisTemplate<String, Object> persistentRedisTemplate,
                                                  PersistentProperties properties) {
            PersistentProperties.RedisConfig redisConfig = properties.getCache().getRedis();
            PersistentProperties.CaffeineConfig caffeineConfig = properties.getCache().getCaffeine();

            log.info("Creating Two-Level CacheManager (L1: Caffeine, L2: Redis)");

            CaffeineCacheManager l1 = new CaffeineCacheManager(
                    caffeineConfig.getMaxSize(),
                    caffeineConfig.getExpireAfterWrite());

            RedisCacheManager l2 = new RedisCacheManager(
                    persistentRedisTemplate,
                    redisConfig.getKeyPrefix(),
                    redisConfig.getDefaultTtl());

            return new TwoLevelCacheManager(l1, l2);
        }
    }

    /**
     * 无缓存配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "markeb.persistent.cache", name = "type", havingValue = "none")
    public static class NoneCacheConfiguration {

        @Bean
        @ConditionalOnMissingBean(CacheManager.class)
        public CacheManager noneCacheManager() {
            log.info("Creating None CacheManager (cache disabled)");
            return new NoneCacheManager();
        }
    }
}

