package org.markeb.actor;

import java.util.concurrent.CompletableFuture;

/**
 * Actor 上下文
 * <p>
 * 提供 Actor 处理消息时可用的上下文信息和操作。
 * </p>
 */
public interface ActorContext {

    /**
     * 获取当前 Actor 的引用
     *
     * @return 当前 Actor
     */
    Actor self();

    /**
     * 获取 ActorSystem
     *
     * @return ActorSystem 实例
     */
    ActorSystem system();

    /**
     * 获取消息发送者（如果有）
     * <p>
     * 对于 ask 模式的消息，可以通过此方法获取发送者以便回复。
     * </p>
     *
     * @return 发送者 Actor，如果没有则返回 null
     */
    Actor sender();

    /**
     * 设置用于 ask 模式的响应
     *
     * @param response 响应对象
     * @param <T>      响应类型
     */
    <T> void reply(T response);

    /**
     * 获取当前消息的 CompletableFuture（用于 ask 模式）
     *
     * @param <T> 响应类型
     * @return CompletableFuture，如果是 tell 模式则返回 null
     */
    <T> CompletableFuture<T> future();

    /**
     * 调度延迟消息
     *
     * @param message 消息
     * @param delayMs 延迟毫秒数
     */
    void scheduleOnce(Object message, long delayMs);

    /**
     * 调度周期性消息
     *
     * @param message    消息
     * @param initialDelayMs 初始延迟毫秒数
     * @param periodMs   周期毫秒数
     * @return 调度任务的 ID，可用于取消
     */
    String schedulePeriodic(Object message, long initialDelayMs, long periodMs);

    /**
     * 取消周期性调度
     *
     * @param scheduleId 调度任务 ID
     */
    void cancelSchedule(String scheduleId);

}

