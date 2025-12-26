package org.markeb.locate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.markeb.locate.LocateService;
import org.markeb.locate.local.LocalLocateService;
import org.markeb.locate.redis.RedisLocateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 定位服务自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(LocateProperties.class)
@ConditionalOnProperty(prefix = "markeb.locate", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LocateAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LocateAutoConfiguration.class);

    @Configuration
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnProperty(prefix = "markeb.locate", name = "type", havingValue = "REDIS", matchIfMissing = true)
    static class RedisLocateConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public LocateService redisLocateService(StringRedisTemplate redisTemplate,
                                                 ObjectMapper objectMapper,
                                                 LocateProperties properties) {
            log.info("Initializing Redis locate service with expire time: {}", properties.getExpireTime());
            return new RedisLocateService(redisTemplate, objectMapper, properties.getExpireTime());
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "markeb.locate", name = "type", havingValue = "LOCAL")
    static class LocalLocateConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public LocateService localLocateService() {
            log.info("Initializing Local locate service (for testing only)");
            return new LocalLocateService();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

