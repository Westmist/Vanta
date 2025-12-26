package org.markeb.actor;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Actor 系统接口
 * <p>
 * ActorSystem 是 Actor 模型的核心入口，负责：
 * 1. 创建和管理 Actor
 * 2. 提供 Actor 查找服务
 * 3. 管理执行器生命周期
 * </p>
 */
public interface ActorSystem {

    /**
     * 创建一个新的 Actor
     *
     * @param actorId       Actor 唯一标识
     * @param initialState  初始状态
     * @param behavior      行为定义
     * @param <T>           状态类型
     * @return Actor 引用
     */
    <T> ActorRef spawn(long actorId, T initialState, ActorBehavior<T> behavior);

    /**
     * 使用自定义配置创建 Actor
     *
     * @param actorId       Actor 唯一标识
     * @param initialState  初始状态
     * @param behavior      行为定义
     * @param config        Actor 配置
     * @param <T>           状态类型
     * @return Actor 引用
     */
    <T> ActorRef spawn(long actorId, T initialState, ActorBehavior<T> behavior, ActorConfig config);

    /**
     * 根据 ID 查找 Actor
     *
     * @param actorId Actor 唯一标识
     * @return Actor 引用，如果不存在返回 Optional.empty()
     */
    Optional<ActorRef> lookup(long actorId);

    /**
     * 获取或创建 Actor
     * <p>
     * 如果 Actor 已存在则返回现有引用，否则创建新的 Actor。
     * </p>
     *
     * @param actorId       Actor 唯一标识
     * @param initialState  初始状态（仅在创建时使用）
     * @param behavior      行为定义（仅在创建时使用）
     * @param <T>           状态类型
     * @return Actor 引用
     */
    <T> ActorRef getOrSpawn(long actorId, T initialState, ActorBehavior<T> behavior);

    /**
     * 停止指定的 Actor
     *
     * @param actorId Actor 唯一标识
     * @return 如果 Actor 存在并被停止返回 true
     */
    boolean stop(long actorId);

    /**
     * 向指定 Actor 发送消息
     * <p>
     * 如果 Actor 不存在，消息将被丢弃。
     * </p>
     *
     * @param actorId Actor 唯一标识
     * @param message 消息
     * @return 如果消息成功发送返回 true
     */
    boolean tell(long actorId, Object message);

    /**
     * 向指定 Actor 发送消息并等待响应
     *
     * @param actorId Actor 唯一标识
     * @param message 消息
     * @param <T>     响应类型
     * @return CompletableFuture，如果 Actor 不存在则返回失败的 Future
     */
    <T> CompletableFuture<T> ask(long actorId, Object message);

    /**
     * 获取当前 Actor 数量
     *
     * @return Actor 数量
     */
    int actorCount();

    /**
     * 获取执行器类型
     *
     * @return 执行器类型
     */
    ExecutorType getExecutorType();

    /**
     * 关闭 Actor 系统
     * <p>
     * 停止所有 Actor 并关闭执行器。
     * </p>
     */
    void shutdown();

    /**
     * 等待 Actor 系统关闭
     *
     * @param timeoutMs 超时毫秒数
     * @return 如果在超时前关闭返回 true
     * @throws InterruptedException 如果等待时被中断
     */
    boolean awaitTermination(long timeoutMs) throws InterruptedException;

}

