package org.markeb.meta;

import org.markeb.meta.luban.IBean;
import org.markeb.meta.luban.ITable;

import java.util.function.Supplier;

/**
 * 配置表注册接口
 * 用于注册 Luban 生成的配置表类
 */
public interface TableRegistry {

    /**
     * 注册配置表
     *
     * @param tableClass 表类型
     * @param factory    表工厂
     * @param <K>        主键类型
     * @param <V>        值类型
     * @param <T>        表类型
     */
    <K, V extends IBean, T extends ITable<K, V>> void register(Class<T> tableClass, Supplier<T> factory);
}

