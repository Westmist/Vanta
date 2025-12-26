package org.markeb.persistent;

import org.markeb.persistent.entity.Identifiable;
import org.markeb.persistent.service.PersistentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * 数据中心门面类
 * 提供静态方法访问持久化服务，方便业务代码使用
 */
public class DataCenter {

    private static final Logger log = LoggerFactory.getLogger(DataCenter.class);

    private static PersistentService service;

    public DataCenter(PersistentService service) {
        DataCenter.service = service;
        log.info("DataCenter initialized with PersistentService");
    }

    /**
     * 获取持久化服务实例
     */
    public static PersistentService getService() {
        return service;
    }

    /**
     * 根据ID查找实体
     */
    public static <T extends Identifiable<ID>, ID> Optional<T> find(Class<T> clazz, ID id) {
        return service.find(clazz, id);
    }

    /**
     * 保存实体（同步）
     */
    public static <T extends Identifiable<ID>, ID> T save(T entity) {
        return service.save(entity);
    }

    /**
     * 保存实体（异步）
     */
    public static <T extends Identifiable<ID>, ID> void saveAsync(T entity) {
        service.saveAsync(entity);
    }

    /**
     * 删除实体
     */
    public static <T extends Identifiable<ID>, ID> void delete(T entity) {
        service.delete(entity);
    }

    /**
     * 根据ID删除实体
     */
    public static <T extends Identifiable<ID>, ID> void deleteById(Class<T> clazz, ID id) {
        service.deleteById(clazz, id);
    }

    /**
     * 查询所有实体
     */
    public static <T extends Identifiable<ID>, ID> List<T> findAll(Class<T> clazz) {
        return service.findAll(clazz);
    }

    /**
     * 判断实体是否存在
     */
    public static <T extends Identifiable<ID>, ID> boolean exists(Class<T> clazz, ID id) {
        return service.exists(clazz, id);
    }

    /**
     * 刷新缓存
     */
    public static <T extends Identifiable<ID>, ID> Optional<T> refresh(Class<T> clazz, ID id) {
        return service.refresh(clazz, id);
    }

    /**
     * 清除缓存
     */
    public static <T extends Identifiable<ID>, ID> void evictCache(Class<T> clazz, ID id) {
        service.evictCache(clazz, id);
    }

}
