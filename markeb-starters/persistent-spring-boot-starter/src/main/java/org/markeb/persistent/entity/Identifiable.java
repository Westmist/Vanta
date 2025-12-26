package org.markeb.persistent.entity;

/**
 * 可标识实体接口
 * 所有持久化实体必须实现此接口
 *
 * @param <ID> 主键类型
 */
public interface Identifiable<ID> {

    /**
     * 获取实体ID
     */
    ID getId();

    /**
     * 设置实体ID
     */
    void setId(ID id);
}

