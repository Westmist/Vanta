package org.markeb.meta;

import org.springframework.context.ApplicationEvent;

import java.util.Set;

/**
 * 配置表热更新事件
 * 当配置表重新加载时触发
 */
public class MetaReloadEvent extends ApplicationEvent {

    private final Set<String> reloadedTables;
    private final boolean fullReload;

    /**
     * 创建完整重载事件
     *
     * @param source 事件源
     */
    public MetaReloadEvent(Object source) {
        super(source);
        this.reloadedTables = Set.of();
        this.fullReload = true;
    }

    /**
     * 创建部分重载事件
     *
     * @param source         事件源
     * @param reloadedTables 重载的表名集合
     */
    public MetaReloadEvent(Object source, Set<String> reloadedTables) {
        super(source);
        this.reloadedTables = reloadedTables;
        this.fullReload = false;
    }

    /**
     * 获取重载的表名集合
     *
     * @return 表名集合，完整重载时返回空集合
     */
    public Set<String> getReloadedTables() {
        return reloadedTables;
    }

    /**
     * 是否为完整重载
     *
     * @return 是否完整重载
     */
    public boolean isFullReload() {
        return fullReload;
    }

    /**
     * 检查指定表是否被重载
     *
     * @param tableName 表名
     * @return 是否被重载
     */
    public boolean isTableReloaded(String tableName) {
        return fullReload || reloadedTables.contains(tableName);
    }
}

