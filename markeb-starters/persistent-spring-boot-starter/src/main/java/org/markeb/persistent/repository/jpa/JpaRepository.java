package org.markeb.persistent.repository.jpa;

import org.markeb.persistent.entity.Identifiable;
import org.markeb.persistent.repository.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * JPA 仓储实现
 */
public class JpaRepository<T extends Identifiable<ID>, ID> implements Repository<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(JpaRepository.class);

    private final EntityManager entityManager;

    public JpaRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<T> findById(ID id) {
        throw new UnsupportedOperationException("Use findById(Class<E>, ID) instead");
    }

    @Override
    public <E extends T> Optional<E> findById(Class<E> clazz, ID id) {
        E entity = entityManager.find(clazz, id);
        return Optional.ofNullable(entity);
    }

    @Override
    public <E extends T> E save(E entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
            return entity;
        } else {
            return entityManager.merge(entity);
        }
    }

    @Override
    public <E extends T> void delete(E entity) {
        if (entityManager.contains(entity)) {
            entityManager.remove(entity);
        } else {
            E merged = entityManager.merge(entity);
            entityManager.remove(merged);
        }
    }

    @Override
    public <E extends T> void deleteById(Class<E> clazz, ID id) {
        findById(clazz, id).ifPresent(this::delete);
    }

    @Override
    public <E extends T> List<E> findAll(Class<E> clazz) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(clazz);
        Root<E> root = cq.from(clazz);
        cq.select(root);
        TypedQuery<E> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    @Override
    public <E extends T> boolean existsById(Class<E> clazz, ID id) {
        return findById(clazz, id).isPresent();
    }

    @Override
    public <E extends T> long count(Class<E> clazz) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        cq.select(cb.count(cq.from(clazz)));
        return entityManager.createQuery(cq).getSingleResult();
    }

    /**
     * 获取 EntityManager（用于高级查询）
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }
}

