package org.markeb.meta.luban;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Luban 配置表容器
 * 管理所有配置表的加载和访问
 */
public class Tables {

    private final Map<Class<?>, ITable<?, ?>> tables = new ConcurrentHashMap<>();

    /**
     * 注册配置表
     *
     * @param tableClass 表类型
     * @param table      表实例
     * @param <T>        表类型
     */
    public <T extends ITable<?, ?>> void register(Class<T> tableClass, T table) {
        tables.put(tableClass, table);
    }

    /**
     * 获取配置表
     *
     * @param tableClass 表类型
     * @param <T>        表类型
     * @return 表实例
     */
    @SuppressWarnings("unchecked")
    public <T extends ITable<?, ?>> T get(Class<T> tableClass) {
        return (T) tables.get(tableClass);
    }

    /**
     * 获取配置表（不存在则抛异常）
     *
     * @param tableClass 表类型
     * @param <T>        表类型
     * @return 表实例
     */
    public <T extends ITable<?, ?>> T getOrThrow(Class<T> tableClass) {
        T table = get(tableClass);
        if (table == null) {
            throw new IllegalArgumentException("Table not found: " + tableClass.getName());
        }
        return table;
    }

    /**
     * 获取所有已注册的表
     *
     * @return 所有表
     */
    public Iterable<ITable<?, ?>> getAll() {
        return tables.values();
    }

    /**
     * 清空所有表
     */
    public void clear() {
        tables.clear();
    }

    /**
     * 解析所有表的引用关系
     */
    public void resolveAll() {
        for (ITable<?, ?> table : tables.values()) {
            table.resolve(this);
        }
    }
}

