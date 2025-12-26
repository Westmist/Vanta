package org.markeb.actor.mailbox;

/**
 * Actor 邮箱接口
 * <p>
 * 邮箱负责存储发送给 Actor 的消息，保证消息的 FIFO 顺序。
 * </p>
 */
public interface Mailbox {

    /**
     * 将消息放入邮箱
     *
     * @param envelope 消息信封
     * @return 如果成功放入返回 true，如果邮箱已满或已关闭返回 false
     */
    boolean enqueue(Envelope envelope);

    /**
     * 从邮箱取出消息（阻塞）
     *
     * @return 消息信封，如果邮箱已关闭返回 null
     * @throws InterruptedException 如果等待时被中断
     */
    Envelope dequeue() throws InterruptedException;

    /**
     * 尝试从邮箱取出消息（非阻塞）
     *
     * @return 消息信封，如果邮箱为空返回 null
     */
    Envelope tryDequeue();

    /**
     * 获取邮箱中的消息数量
     *
     * @return 消息数量
     */
    int size();

    /**
     * 检查邮箱是否为空
     *
     * @return 如果邮箱为空返回 true
     */
    boolean isEmpty();

    /**
     * 关闭邮箱
     * <p>
     * 关闭后不再接收新消息，但已有消息可以继续处理。
     * </p>
     */
    void close();

    /**
     * 检查邮箱是否已关闭
     *
     * @return 如果邮箱已关闭返回 true
     */
    boolean isClosed();

}

