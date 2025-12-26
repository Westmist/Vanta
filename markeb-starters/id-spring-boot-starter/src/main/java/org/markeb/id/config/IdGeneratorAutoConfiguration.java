package org.markeb.id.config;

import org.markeb.id.IdGenerator;
import org.markeb.id.YitIdGenerator;
import org.markeb.id.worker.RedisWorkerIdAssigner;
import org.markeb.id.worker.WorkerIdAssigner;
import com.github.yitter.contract.IdGeneratorOptions;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * ID 生成器自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(IdGeneratorProperties.class)
@ConditionalOnProperty(prefix = "markeb.id", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IdGeneratorAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(IdGeneratorAutoConfiguration.class);

    /**
     * Redis WorkerId 分配器（自动注册时使用）
     */
    @Configuration
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnProperty(prefix = "markeb.id", name = "auto-register", havingValue = "true")
    static class RedisWorkerIdConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public WorkerIdAssigner redisWorkerIdAssigner(StringRedisTemplate redisTemplate,
                                                       IdGeneratorProperties properties) {
            log.info("Initializing Redis WorkerId assigner");
            return new RedisWorkerIdAssigner(redisTemplate, properties);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public IdGenerator idGenerator(IdGeneratorProperties properties,
                                   WorkerIdAssigner workerIdAssigner) {
        // 确定 WorkerId
        int workerId;
        if (properties.getWorkerId() >= 0) {
            // 手动指定
            workerId = properties.getWorkerId();
            log.info("Using manually configured WorkerId: {}", workerId);
        } else if (properties.isAutoRegister() && workerIdAssigner != null) {
            // 自动注册
            workerId = workerIdAssigner.assignWorkerId();
            log.info("Auto registered WorkerId: {}", workerId);
        } else {
            // 默认使用 1
            workerId = 1;
            log.warn("WorkerId not configured, using default: {}", workerId);
        }

        // 构建配置
        IdGeneratorOptions options = new IdGeneratorOptions((short) workerId);
        options.WorkerIdBitLength = (byte) properties.getWorkerIdBitLength();
        options.SeqBitLength = (byte) properties.getSeqBitLength();
        options.BaseTime = parseBaseTime(properties.getBaseTime());

        log.info("IdGenerator config: WorkerId={}, WorkerIdBitLength={}, SeqBitLength={}, BaseTime={}",
                workerId, options.WorkerIdBitLength, options.SeqBitLength, properties.getBaseTime());

        return new YitIdGenerator(options);
    }

    /**
     * 默认的 WorkerId 分配器（不自动注册时使用）
     */
    @Bean
    @ConditionalOnMissingBean
    public WorkerIdAssigner defaultWorkerIdAssigner(IdGeneratorProperties properties) {
        return () -> properties.getWorkerId() >= 0 ? properties.getWorkerId() : 1;
    }

    /**
     * 解析基准时间
     */
    private long parseBaseTime(String baseTime) {
        try {
            if (baseTime.contains(" ") || baseTime.contains("T")) {
                // 完整时间格式
                LocalDateTime dateTime = LocalDateTime.parse(baseTime,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
            } else {
                // 仅日期格式
                LocalDate date = LocalDate.parse(baseTime, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
            }
        } catch (Exception e) {
            log.warn("Failed to parse baseTime '{}', using default 2024-01-01", baseTime);
            return LocalDate.of(2024, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
        }
    }
}

