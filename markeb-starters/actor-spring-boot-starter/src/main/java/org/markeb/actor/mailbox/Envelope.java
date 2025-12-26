package org.markeb.actor.mailbox;

import org.markeb.actor.ActorRef;

import java.util.concurrent.CompletableFuture;

/**
 * 消息信封
 * <p>
 * 包装发送给 Actor 的消息，包含消息本身和元数据。
 * </p>
 */
public class Envelope {

    private final Object message;
    private final ActorRef sender;
    private final CompletableFuture<Object> future;
    private final long timestamp;

    /**
     * 创建 tell 模式的信封
     */
    public Envelope(Object message, ActorRef sender) {
        this(message, sender, null);
    }

    /**
     * 创建 ask 模式的信封
     */
    public Envelope(Object message, ActorRef sender, CompletableFuture<Object> future) {
        this.message = message;
        this.sender = sender;
        this.future = future;
        this.timestamp = System.currentTimeMillis();
    }

    public Object getMessage() {
        return message;
    }

    public ActorRef getSender() {
        return sender;
    }

    public CompletableFuture<Object> getFuture() {
        return future;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 是否是 ask 模式的消息
     */
    public boolean isAsk() {
        return future != null;
    }

    /**
     * 完成 ask 请求
     */
    public void complete(Object result) {
        if (future != null) {
            future.complete(result);
        }
    }

    /**
     * 以异常完成 ask 请求
     */
    public void completeExceptionally(Throwable throwable) {
        if (future != null) {
            future.completeExceptionally(throwable);
        }
    }

    @Override
    public String toString() {
        return "Envelope{" +
                "message=" + message.getClass().getSimpleName() +
                ", sender=" + (sender != null ? sender.actorId() : "null") +
                ", isAsk=" + isAsk() +
                '}';
    }
}

