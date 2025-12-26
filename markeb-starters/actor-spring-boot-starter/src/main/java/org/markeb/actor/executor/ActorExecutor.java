package org.markeb.actor.executor;

import org.markeb.actor.mailbox.Envelope;

/**
 * Actor 执行器接口
 * <p>
 * 负责调度和执行 Actor 的消息处理任务。
 * </p>
 */
public interface ActorExecutor {

    /**
     * 提交消息处理任务
     *
     * @param actorId  Actor 标识
     * @param envelope 消息信封
     * @param task     处理任务
     */
    void execute(String actorId, Envelope envelope, Runnable task);

    /**
     * 调度延迟任务
     *
     * @param actorId Actor 标识
     * @param task    任务
     * @param delayMs 延迟毫秒数
     */
    void schedule(String actorId, Runnable task, long delayMs);

    /**
     * 调度周期性任务
     *
     * @param actorId        Actor 标识
     * @param task           任务
     * @param initialDelayMs 初始延迟毫秒数
     * @param periodMs       周期毫秒数
     * @return 调度任务 ID
     */
    String schedulePeriodic(String actorId, Runnable task, long initialDelayMs, long periodMs);

    /**
     * 取消周期性任务
     *
     * @param scheduleId 调度任务 ID
     */
    void cancelSchedule(String scheduleId);

    /**
     * 关闭执行器
     */
    void shutdown();

    /**
     * 等待执行器关闭
     *
     * @param timeoutMs 超时毫秒数
     * @return 如果在超时前关闭返回 true
     * @throws InterruptedException 如果等待时被中断
     */
    boolean awaitTermination(long timeoutMs) throws InterruptedException;

}

