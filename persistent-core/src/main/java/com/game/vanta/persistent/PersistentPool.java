package com.game.vanta.persistent;

import com.game.vanta.persistent.dao.IPersistent;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Map;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class PersistentPool {

  private final BiMap<String, Class<? extends IPersistent>> persistentPool = HashBiMap.create();

  @SuppressWarnings("unchecked")
  public PersistentPool(ApplicationContext context, MongoTemplate mongoTemplate) {
    Map<String, IPersistent> beans = context.getBeansOfType(IPersistent.class);
    for (IPersistent persistent : beans.values()) {
      Class<? extends IPersistent> clazz =
          (Class<? extends IPersistent>) AopUtils.getTargetClass(persistent);
      String collectionName = mongoTemplate.getCollectionName(clazz);
      persistentPool.put(collectionName, clazz);
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
