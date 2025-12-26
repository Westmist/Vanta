package org.markeb.persistent.service;

import org.markeb.common.scanner.ClassScanner;
import org.markeb.persistent.annotation.PersistentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体元数据注册表
 * 管理所有持久化实体的元数据
 */
public class EntityMetadataRegistry {

    private static final Logger log = LoggerFactory.getLogger(EntityMetadataRegistry.class);

    private final Map<Class<?>, EntityMetadata> metadataByClass = new ConcurrentHashMap<>();
    private final Map<String, EntityMetadata> metadataByCollection = new ConcurrentHashMap<>();

    /**
     * 扫描并注册实体
     *
     * @param basePackages 扫描的包路径
     */
    public void scan(String... basePackages) {
        for (String basePackage : basePackages) {
            if (basePackage == null || basePackage.isEmpty()) {
                continue;
            }

            Set<Class<?>> classes = ClassScanner.builder()
                    .basePackages(basePackage)
                    .byAnnotation(PersistentEntity.class)
                    .scan();

            for (Class<?> clazz : classes) {
                register(clazz);
            }
        }
    }

    /**
     * 注册实体类
     */
    public void register(Class<?> entityClass) {
        EntityMetadata metadata = new EntityMetadata(entityClass);
        metadataByClass.put(entityClass, metadata);
        metadataByCollection.put(metadata.getCollection(), metadata);
        log.info("Registered persistent entity: {} -> {}", entityClass.getSimpleName(), metadata.getCollection());
    }

    /**
     * 获取实体元数据
     */
    public EntityMetadata getMetadata(Class<?> entityClass) {
        return metadataByClass.computeIfAbsent(entityClass, EntityMetadata::new);
    }

    /**
     * 根据集合名获取元数据
     */
    public EntityMetadata getMetadataByCollection(String collection) {
        return metadataByCollection.get(collection);
    }

    /**
     * 获取实体类
     */
    public Class<?> getEntityClass(String collection) {
        EntityMetadata metadata = metadataByCollection.get(collection);
        return metadata != null ? metadata.getEntityClass() : null;
    }

    /**
     * 判断是否已注册
     */
    public boolean isRegistered(Class<?> entityClass) {
        return metadataByClass.containsKey(entityClass);
    }

}

