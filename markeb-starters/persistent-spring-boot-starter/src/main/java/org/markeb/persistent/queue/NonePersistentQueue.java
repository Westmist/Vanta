package org.markeb.persistent.queue;

import java.util.concurrent.CompletableFuture;

/**
 * 无队列实现（同步模式）
 * 直接调用 handler 处理消息
 */
public class NonePersistentQueue implements PersistentQueue {

    private PersistentMessageHandler handler;

    @Override
    public QueueType getType() {
        return QueueType.NONE;
    }

    @Override
    public CompletableFuture<Void> sendAsync(PersistentMessage message) {
        return CompletableFuture.runAsync(() -> {
            if (handler != null) {
                handler.handle(message);
            }
        });
    }

    @Override
    public void sendSync(PersistentMessage message) {
        if (handler != null) {
            handler.handle(message);
        }
    }

    @Override
    public void subscribe(PersistentMessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void start() {
        // no-op
    }

    @Override
    public void stop() {
        // no-op
    }
}

