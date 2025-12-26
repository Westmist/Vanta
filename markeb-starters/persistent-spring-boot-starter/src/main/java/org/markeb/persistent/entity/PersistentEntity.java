package org.markeb.persistent.entity;

import java.io.Serializable;

/**
 * 持久化实体基类
 * 提供通用的实体功能，业务实体可选择继承
 */
public abstract class PersistentEntity implements Serializable {

    /**
     * 获取实体ID
     */
    public abstract String getId();

    /**
     * 设置实体ID
     */
    public abstract void setId(String id);

    /**
     * 生成缓存键
     */
    public String cacheKey() {
        return getClass().getSimpleName().toLowerCase() + ":" + getId();
    }

}

