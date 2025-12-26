package org.markeb.persistent.repository.mongo;

import org.markeb.persistent.entity.Identifiable;
import org.markeb.persistent.repository.Repository;
import org.markeb.persistent.repository.RepositoryFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.concurrent.ConcurrentHashMap;

/**
 * MongoDB 仓储工厂
 */
public class MongoRepositoryFactory implements RepositoryFactory {

    private final MongoTemplate mongoTemplate;
    private final ConcurrentHashMap<Class<?>, Repository<?, ?>> repositoryCache = new ConcurrentHashMap<>();

    public MongoRepositoryFactory(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Identifiable<ID>, ID> Repository<T, ID> getRepository(Class<T> entityClass) {
        return (Repository<T, ID>) repositoryCache.computeIfAbsent(
                entityClass,
                clazz -> new MongoRepository<>(mongoTemplate)
        );
    }

    @Override
    public boolean supports(Class<?> entityClass) {
        // MongoDB 支持任何类型的实体
        return true;
    }

    /**
     * 获取集合名称
     */
    public String getCollectionName(Class<?> entityClass) {
        return mongoTemplate.getCollectionName(entityClass);
    }

}

