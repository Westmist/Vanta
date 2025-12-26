package org.markeb.config;

/**
 * 配置变更事件
 */
public class ConfigChangeEvent {

    /**
     * 配置键
     */
    private final String key;

    /**
     * 旧值
     */
    private final String oldValue;

    /**
     * 新值
     */
    private final String newValue;

    /**
     * 变更类型
     */
    private final ChangeType changeType;

    public ConfigChangeEvent(String key, String oldValue, String newValue, ChangeType changeType) {
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeType = changeType;
    }

    public String getKey() {
        return key;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public enum ChangeType {
        /**
         * 新增
         */
        ADD,

        /**
         * 修改
         */
        MODIFY,

        /**
         * 删除
         */
        DELETE
    }

    @Override
    public String toString() {
        return "ConfigChangeEvent{" +
                "key='" + key + '\'' +
                ", changeType=" + changeType +
                '}';
    }
}

