package org.markeb.meta.luban;

/**
 * Luban 配置表接口
 * 所有 Luban 生成的表类都需要实现此接口
 *
 * @param <K> 主键类型
 * @param <V> 值类型（Bean类型）
 */
public interface ITable<K, V extends IBean> {

    /**
     * 获取表名
     *
     * @return 表名
     */
    String getTableName();

    /**
     * 根据主键获取配置
     *
     * @param key 主键
     * @return 配置对象，不存在返回 null
     */
    V get(K key);

    /**
     * 根据主键获取配置（不存在则抛异常）
     *
     * @param key 主键
     * @return 配置对象
     * @throws IllegalArgumentException 如果配置不存在
     */
    default V getOrThrow(K key) {
        V value = get(key);
        if (value == null) {
            throw new IllegalArgumentException("Config not found: " + getTableName() + "[" + key + "]");
        }
        return value;
    }

    /**
     * 检查配置是否存在
     *
     * @param key 主键
     * @return 是否存在
     */
    boolean contains(K key);

    /**
     * 获取所有配置
     *
     * @return 所有配置的可迭代对象
     */
    Iterable<V> getAll();

    /**
     * 获取配置数量
     *
     * @return 配置数量
     */
    int size();

    /**
     * 从二进制数据加载
     *
     * @param buf 二进制缓冲区
     */
    void load(ByteBuf buf);

    /**
     * 解析完成后的回调（用于建立引用关系等）
     *
     * @param tables 所有表的容器
     */
    default void resolve(Tables tables) {
    }
}

