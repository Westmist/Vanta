package org.markeb.persistent.repository;

import org.markeb.persistent.entity.Identifiable;

/**
 * 仓储工厂接口
 * 用于创建特定类型的 Repository 实例
 */
public interface RepositoryFactory {

    /**
     * 获取指定实体类型的 Repository
     *
     * @param entityClass 实体类
     * @param <T>         实体类型
     * @param <ID>        主键类型
     * @return Repository 实例
     */
    <T extends Identifiable<ID>, ID> Repository<T, ID> getRepository(Class<T> entityClass);

    /**
     * 判断是否支持该实体类型
     */
    boolean supports(Class<?> entityClass);

}

