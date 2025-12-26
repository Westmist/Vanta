package org.markeb.persistent.queue;

import java.util.concurrent.CompletableFuture;

/**
 * 持久化队列接口
 */
public interface PersistentQueue {

    /**
     * 获取队列类型
     */
    QueueType getType();

    /**
     * 异步发送消息
     *
     * @param message 消息
     * @return CompletableFuture
     */
    CompletableFuture<Void> sendAsync(PersistentMessage message);

    /**
     * 同步发送消息
     *
     * @param message 消息
     */
    void sendSync(PersistentMessage message);

    /**
     * 订阅消息
     *
     * @param handler 消息处理器
     */
    void subscribe(PersistentMessageHandler handler);

    /**
     * 启动队列
     */
    void start();

    /**
     * 停止队列
     */
    void stop();
}
