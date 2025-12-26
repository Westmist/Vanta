package org.markeb.persistent.repository;

import org.markeb.persistent.entity.Identifiable;

import java.util.List;
import java.util.Optional;

/**
 * 仓储接口
 * 抽象数据库操作
 *
 * @param <T>  实体类型
 * @param <ID> 主键类型
 */
public interface Repository<T extends Identifiable<ID>, ID> {

    /**
     * 根据ID查找实体
     *
     * @param id 实体ID
     * @return 实体（可能为空）
     */
    Optional<T> findById(ID id);

    /**
     * 根据ID查找实体
     *
     * @param clazz 实体类型
     * @param id    实体ID
     * @return 实体（可能为空）
     */
    <E extends T> Optional<E> findById(Class<E> clazz, ID id);

    /**
     * 保存实体（新增或更新）
     *
     * @param entity 实体
     * @return 保存后的实体
     */
    <E extends T> E save(E entity);

    /**
     * 删除实体
     *
     * @param entity 实体
     */
    <E extends T> void delete(E entity);

    /**
     * 根据ID删除实体
     *
     * @param clazz 实体类型
     * @param id    实体ID
     */
    <E extends T> void deleteById(Class<E> clazz, ID id);

    /**
     * 查询所有实体
     *
     * @param clazz 实体类型
     * @return 实体列表
     */
    <E extends T> List<E> findAll(Class<E> clazz);

    /**
     * 判断实体是否存在
     *
     * @param clazz 实体类型
     * @param id    实体ID
     * @return 是否存在
     */
    <E extends T> boolean existsById(Class<E> clazz, ID id);

    /**
     * 统计实体数量
     *
     * @param clazz 实体类型
     * @return 数量
     */
    <E extends T> long count(Class<E> clazz);
}
