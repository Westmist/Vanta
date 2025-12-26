package org.markeb.persistent.service;

import org.markeb.persistent.entity.Identifiable;

import java.util.List;
import java.util.Optional;

/**
 * 持久化服务接口
 */
public interface PersistentService {

    /**
     * 根据ID查找实体（先查缓存，再查数据库）
     *
     * @param clazz 实体类型
     * @param id    实体ID
     * @return 实体（可能为空）
     */
    <T extends Identifiable<ID>, ID> Optional<T> find(Class<T> clazz, ID id);

    /**
     * 同步保存实体（直接写数据库和缓存）
     *
     * @param entity 实体
     * @return 保存后的实体
     */
    <T extends Identifiable<ID>, ID> T save(T entity);

    /**
     * 异步保存实体（先写缓存，异步写数据库）
     *
     * @param entity 实体
     */
    <T extends Identifiable<ID>, ID> void saveAsync(T entity);

    /**
     * 删除实体
     *
     * @param entity 实体
     */
    <T extends Identifiable<ID>, ID> void delete(T entity);

    /**
     * 根据ID删除实体
     *
     * @param clazz 实体类型
     * @param id    实体ID
     */
    <T extends Identifiable<ID>, ID> void deleteById(Class<T> clazz, ID id);

    /**
     * 查询所有实体
     *
     * @param clazz 实体类型
     * @return 实体列表
     */
    <T extends Identifiable<ID>, ID> List<T> findAll(Class<T> clazz);

    /**
     * 判断实体是否存在
     *
     * @param clazz 实体类型
     * @param id    实体ID
     * @return 是否存在
     */
    <T extends Identifiable<ID>, ID> boolean exists(Class<T> clazz, ID id);

    /**
     * 刷新缓存（从数据库重新加载）
     *
     * @param clazz 实体类型
     * @param id    实体ID
     * @return 刷新后的实体
     */
    <T extends Identifiable<ID>, ID> Optional<T> refresh(Class<T> clazz, ID id);

    /**
     * 清除缓存
     *
     * @param clazz 实体类型
     * @param id    实体ID
     */
    <T extends Identifiable<ID>, ID> void evictCache(Class<T> clazz, ID id);
}
