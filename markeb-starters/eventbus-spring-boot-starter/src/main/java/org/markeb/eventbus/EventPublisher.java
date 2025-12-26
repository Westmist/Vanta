package org.markeb.eventbus;

import java.util.concurrent.CompletableFuture;

/**
 * 事件发布者接口
 */
public interface EventPublisher {

    /**
     * 获取事件总线类型
     */
    EventBusType getType();

    /**
     * 同步发布事件
     *
     * @param event 事件
     */
    void publish(Event event);

    /**
     * 异步发布事件
     *
     * @param event 事件
     * @return CompletableFuture
     */
    CompletableFuture<Void> publishAsync(Event event);

    /**
     * 发布事件到指定主题
     *
     * @param topic 主题
     * @param event 事件
     */
    void publish(String topic, Event event);

    /**
     * 异步发布事件到指定主题
     *
     * @param topic 主题
     * @param event 事件
     * @return CompletableFuture
     */
    CompletableFuture<Void> publishAsync(String topic, Event event);
}

