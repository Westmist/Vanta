package org.markeb.persistent.config;

import org.markeb.persistent.entity.Identifiable;
import org.markeb.persistent.repository.Repository;
import org.markeb.persistent.repository.jpa.JpaRepository;
import org.markeb.persistent.repository.mongo.MongoRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * 仓储层自动配置
 */
@Configuration
public class RepositoryAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RepositoryAutoConfiguration.class);

    /**
     * MongoDB 仓储配置
     */
    @Configuration
    @ConditionalOnClass(MongoTemplate.class)
    @ConditionalOnProperty(prefix = "markeb.persistent.storage", name = "type", havingValue = "mongo", matchIfMissing = true)
    public static class MongoRepositoryConfiguration {

        @Bean
        @ConditionalOnMissingBean(Repository.class)
        public Repository<Identifiable<Object>, Object> mongoRepository(MongoTemplate mongoTemplate) {
            log.info("Creating MongoDB Repository");
            return new MongoRepository<>(mongoTemplate);
        }
    }

    /**
     * JPA 仓储配置
     */
    @Configuration
    @ConditionalOnClass(EntityManager.class)
    @ConditionalOnProperty(prefix = "markeb.persistent.storage", name = "type", havingValue = "jpa")
    public static class JpaRepositoryConfiguration {

        @Bean
        @ConditionalOnMissingBean(Repository.class)
        public Repository<Identifiable<Object>, Object> jpaRepository(EntityManager entityManager) {
            log.info("Creating JPA Repository");
            return new JpaRepository<>(entityManager);
        }
    }
}

