package org.markeb.meta;

/**
 * 配置表注册器接口
 * 实现此接口以注册所有 Luban 生成的配置表
 */
public interface TableRegistrar {

    /**
     * 注册所有配置表
     *
     * @param registry 注册表
     */
    void register(TableRegistry registry);
}

