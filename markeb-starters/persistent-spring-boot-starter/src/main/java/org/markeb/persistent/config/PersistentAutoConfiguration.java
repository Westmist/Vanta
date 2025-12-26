package org.markeb.persistent.config;

import org.markeb.persistent.cache.CacheManager;
import org.markeb.persistent.entity.Identifiable;
import org.markeb.persistent.queue.PersistentQueue;
import org.markeb.persistent.repository.Repository;
import org.markeb.persistent.serialization.EntitySerializer;
import org.markeb.persistent.serialization.ProtostuffEntitySerializer;
import org.markeb.persistent.service.DefaultPersistentService;
import org.markeb.persistent.service.PersistentMessageConsumer;
import org.markeb.persistent.service.PersistentService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * 持久化自动配置入口
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "markeb.persistent", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(PersistentProperties.class)
@Import({
        RepositoryAutoConfiguration.class,
        CacheAutoConfiguration.class,
        QueueAutoConfiguration.class
})
public class PersistentAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(PersistentAutoConfiguration.class);

    private final PersistentProperties properties;

    public PersistentAutoConfiguration(PersistentProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        log.info("Persistent module initialized with storage: {}, cache: {}, queue: {}",
                properties.getStorage().getType(),
                properties.getCache().getType(),
                properties.getQueue().getType());
    }

    @Bean
    @ConditionalOnMissingBean
    public EntitySerializer entitySerializer() {
        return new ProtostuffEntitySerializer();
    }

    @Bean
    @ConditionalOnMissingBean
    public PersistentService persistentService(Repository<Identifiable<Object>, Object> repository,
                                                CacheManager cacheManager,
                                                PersistentQueue persistentQueue,
                                                EntitySerializer entitySerializer) {
        log.info("Creating PersistentService");
        return new DefaultPersistentService(
                repository,
                cacheManager,
                persistentQueue,
                entitySerializer,
                properties.getCache().getRedis().getDefaultTtl()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public PersistentMessageConsumer persistentMessageConsumer(Repository<Identifiable<Object>, Object> repository,
                                                                EntitySerializer entitySerializer,
                                                                PersistentQueue persistentQueue) {
        PersistentMessageConsumer consumer = new PersistentMessageConsumer(repository, entitySerializer);

        // 订阅队列消息
        persistentQueue.subscribe(consumer);
        persistentQueue.start();

        log.info("PersistentMessageConsumer started");
        return consumer;
    }
}
