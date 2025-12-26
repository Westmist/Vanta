package org.markeb.eventbus;

/**
 * 事件处理器接口
 *
 * @param <T> 事件类型
 */
@FunctionalInterface
public interface EventHandler<T extends Event> {

    /**
     * 处理事件
     *
     * @param event 事件
     */
    void handle(T event) throws Exception;
}

