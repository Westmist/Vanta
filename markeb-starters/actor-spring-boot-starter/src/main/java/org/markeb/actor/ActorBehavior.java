package org.markeb.actor;

/**
 * Actor 行为接口
 * <p>
 * 定义 Actor 如何处理接收到的消息。
 * 用户可以通过实现此接口来定义自己的 Actor 逻辑。
 * </p>
 *
 * @param <T> Actor 的状态类型
 */
@FunctionalInterface
public interface ActorBehavior<T> {

    /**
     * 处理接收到的消息
     *
     * @param context 上下文，提供 Actor 相关操作
     * @param state   当前 Actor 的状态
     * @param message 接收到的消息
     * @return 新的状态（可以返回相同的状态对象表示状态未变化）
     * @throws Exception 处理过程中的异常
     */
    T onMessage(ActorContext context, T state, Object message) throws Exception;

}

