package org.markeb.persistent.queue;

/**
 * 持久化消息处理器接口
 */
@FunctionalInterface
public interface PersistentMessageHandler {

    /**
     * 处理持久化消息
     *
     * @param message 消息
     */
    void handle(PersistentMessage message);
}
