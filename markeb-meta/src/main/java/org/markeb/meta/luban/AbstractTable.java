package org.markeb.meta.luban;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Luban 配置表抽象基类
 * 提供通用的表操作实现
 *
 * @param <K> 主键类型
 * @param <V> 值类型（Bean类型）
 */
public abstract class AbstractTable<K, V extends IBean> implements ITable<K, V> {

    protected final Map<K, V> dataMap = new LinkedHashMap<>();
    protected final String tableName;

    protected AbstractTable(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public V get(K key) {
        return dataMap.get(key);
    }

    @Override
    public boolean contains(K key) {
        return dataMap.containsKey(key);
    }

    @Override
    public Iterable<V> getAll() {
        return dataMap.values();
    }

    @Override
    public int size() {
        return dataMap.size();
    }

    @Override
    public void load(ByteBuf buf) {
        dataMap.clear();
        int count = buf.readSize();
        for (int i = 0; i < count; i++) {
            V bean = createBean(buf);
            K key = getKey(bean);
            dataMap.put(key, bean);
        }
    }

    /**
     * 创建 Bean 实例
     *
     * @param buf 二进制缓冲区
     * @return Bean 实例
     */
    protected abstract V createBean(ByteBuf buf);

    /**
     * 获取 Bean 的主键
     *
     * @param bean Bean 实例
     * @return 主键
     */
    protected abstract K getKey(V bean);

    /**
     * 辅助方法：使用工厂函数加载表
     *
     * @param buf         二进制缓冲区
     * @param beanCreator Bean 创建函数
     * @param keyGetter   主键获取函数
     */
    protected void loadWith(ByteBuf buf, Function<ByteBuf, V> beanCreator, Function<V, K> keyGetter) {
        dataMap.clear();
        int count = buf.readSize();
        for (int i = 0; i < count; i++) {
            V bean = beanCreator.apply(buf);
            K key = keyGetter.apply(bean);
            dataMap.put(key, bean);
        }
    }
}

