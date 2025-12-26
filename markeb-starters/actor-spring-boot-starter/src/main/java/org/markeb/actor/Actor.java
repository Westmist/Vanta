package org.markeb.actor;

import java.util.concurrent.CompletableFuture;

/**
 * Actor 接口
 * <p>
 * Actor 是一个独立的计算单元，拥有自己的状态和邮箱。
 * 所有发送给 Actor 的消息都会被串行处理，保证线程安全。
 * </p>
 */
public interface Actor {

    /**
     * 获取 Actor 的唯一标识
     * <p>
     * 该标识用于：
     * 1. 在 ActorSystem 中查找 Actor
     * 2. 决定消息路由到哪个执行器（用于平台线程模式的分片）
     * </p>
     *
     * @return Actor 的唯一标识
     */
    String actorId();

    /**
     * 向该 Actor 发送消息（Fire and Forget）
     * <p>
     * 消息会被放入 Actor 的邮箱，由 Actor 的执行器异步处理。
     * 该方法立即返回，不等待消息处理完成。
     * </p>
     *
     * @param message 要发送的消息
     */
    void tell(Object message);

    /**
     * 向该 Actor 发送消息并等待响应（Ask Pattern）
     * <p>
     * 消息会被放入 Actor 的邮箱，由 Actor 的执行器处理。
     * 返回一个 CompletableFuture，可用于获取处理结果。
     * </p>
     *
     * @param message 要发送的消息
     * @param <T>     响应类型
     * @return 包含响应的 CompletableFuture
     */
    <T> CompletableFuture<T> ask(Object message);

    /**
     * 停止该 Actor
     * <p>
     * 停止后的 Actor 不再接收新消息，邮箱中剩余的消息会被处理完毕。
     * </p>
     */
    void stop();

    /**
     * 检查 Actor 是否已停止
     *
     * @return 如果 Actor 已停止返回 true
     */
    boolean isStopped();

}

