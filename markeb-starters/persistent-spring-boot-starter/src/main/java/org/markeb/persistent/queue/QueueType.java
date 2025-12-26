package org.markeb.persistent.queue;

/**
 * 队列类型枚举
 */
public enum QueueType {

    /**
     * RocketMQ
     */
    ROCKETMQ,

    /**
     * Kafka
     */
    KAFKA,

    /**
     * 内存队列（单机测试用）
     */
    MEMORY,

    /**
     * 禁用队列（同步模式）
     */
    NONE
}

