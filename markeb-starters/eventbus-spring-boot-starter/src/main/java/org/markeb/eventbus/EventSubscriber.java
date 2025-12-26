package org.markeb.eventbus;

/**
 * 事件订阅者接口
 */
public interface EventSubscriber {

    /**
     * 获取事件总线类型
     */
    EventBusType getType();

    /**
     * 订阅主题
     *
     * @param topic   主题
     * @param handler 事件处理器
     */
    void subscribe(String topic, EventHandler<?> handler);

    /**
     * 取消订阅
     *
     * @param topic 主题
     */
    void unsubscribe(String topic);

    /**
     * 启动订阅
     */
    void start();

    /**
     * 停止订阅
     */
    void stop();
}

