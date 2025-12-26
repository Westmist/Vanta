package org.markeb.actor;

import java.util.concurrent.CompletableFuture;

/**
 * Actor 引用
 * <p>
 * 这是对 Actor 的轻量级引用，用于发送消息。
 * ActorRef 可以安全地在线程间传递。
 * </p>
 */
public interface ActorRef {

    /**
     * 获取 Actor 的唯一标识
     *
     * @return Actor ID
     */
    String actorId();

    /**
     * 发送消息（Fire and Forget）
     *
     * @param message 消息
     */
    void tell(Object message);

    /**
     * 发送消息并等待响应
     *
     * @param message 消息
     * @param <T>     响应类型
     * @return CompletableFuture
     */
    <T> CompletableFuture<T> ask(Object message);

    /**
     * 检查 Actor 是否存活
     *
     * @return 如果 Actor 存活返回 true
     */
    boolean isAlive();

}

