package org.markeb.persistent.repository.mongo;

import org.markeb.persistent.entity.Identifiable;
import org.markeb.persistent.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB 仓储实现
 */
public class MongoRepository<T extends Identifiable<ID>, ID> implements Repository<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(MongoRepository.class);

    private final MongoTemplate mongoTemplate;

    public MongoRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<T> findById(ID id) {
        throw new UnsupportedOperationException("Use findById(Class<E>, ID) instead");
    }

    @Override
    public <E extends T> Optional<E> findById(Class<E> clazz, ID id) {
        E entity = mongoTemplate.findById(id, clazz);
        return Optional.ofNullable(entity);
    }

    @Override
    public <E extends T> E save(E entity) {
        return mongoTemplate.save(entity);
    }

    @Override
    public <E extends T> void delete(E entity) {
        mongoTemplate.remove(entity);
    }

    @Override
    public <E extends T> void deleteById(Class<E> clazz, ID id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, clazz);
    }

    @Override
    public <E extends T> List<E> findAll(Class<E> clazz) {
        return mongoTemplate.findAll(clazz);
    }

    @Override
    public <E extends T> boolean existsById(Class<E> clazz, ID id) {
        Query query = Query.query(Criteria.where("_id").is(id));
        return mongoTemplate.exists(query, clazz);
    }

    @Override
    public <E extends T> long count(Class<E> clazz) {
        return mongoTemplate.count(new Query(), clazz);
    }

    /**
     * 获取 MongoTemplate（用于高级查询）
     */
    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }
}
