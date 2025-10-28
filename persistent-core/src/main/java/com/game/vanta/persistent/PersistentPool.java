package com.game.vanta.persistent;

import com.game.vanta.common.scanner.ClassScanner;
import com.game.vanta.persistent.config.PersistentProperties;
import com.game.vanta.persistent.dao.IPersistent;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Set;

public class PersistentPool implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(PersistentPool.class);

    private final BiMap<String, Class<? extends IPersistent>> persistentPool = HashBiMap.create();

    private final PersistentProperties persistentProperties;

    private final MongoTemplate mongoTemplate;

    public PersistentPool(
        PersistentProperties persistentProperties,
        MongoTemplate mongoTemplate) {
        this.persistentProperties = persistentProperties;
        this.mongoTemplate = mongoTemplate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterSingletonsInstantiated() {
        String persistentEntityPackages = persistentProperties.getPersistentEntityPackages();
        if (persistentEntityPackages == null || persistentEntityPackages.isEmpty() || persistentEntityPackages.isBlank()) {
            log.warn("Persistent entity packages is not configured.");
            return;
        }
        Set<Class<?>> persistentClasses = ClassScanner.builder()
            .basePackages(persistentEntityPackages)
            .bySuperType(IPersistent.class)
            .scan();
        for (Class<?> clazz : persistentClasses) {
            Class<? extends IPersistent> persistentClazz = (Class<? extends IPersistent>) clazz;
            String collectionName = mongoTemplate.getCollectionName(persistentClazz);
            persistentPool.put(collectionName, persistentClazz);
        }
    }

    public Class<? extends IPersistent> findClazz(String collectName) {
        return persistentPool.get(collectName);
    }

    public String findCollectName(Class<? extends IPersistent> clazz) {
        return persistentPool.inverse().get(clazz);
    }

    public String persistentKey(Class<? extends IPersistent> clazz, String id) {
        String collectionName = persistentPool.inverse().get(clazz);
        return PersistentUtil.build(collectionName, id);
    }

    public <T extends IPersistent> String persistentKey(T data) {
        return persistentKey(data.getClass(), data.getId());
    }

}
