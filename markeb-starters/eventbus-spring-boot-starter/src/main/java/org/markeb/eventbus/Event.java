package org.markeb.eventbus;

/**
 * 事件接口
 * 所有事件必须实现此接口
 */
public interface Event {

    /**
     * 获取事件主题/频道
     */
    String topic();

    /**
     * 获取事件标签（可选，用于 RocketMQ）
     */
    default String tag() {
        return "*";
    }

    /**
     * 获取事件键（可选，用于分区）
     */
    default String key() {
        return null;
    }
}

