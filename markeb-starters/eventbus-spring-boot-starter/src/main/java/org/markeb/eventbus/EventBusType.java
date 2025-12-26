package org.markeb.eventbus;

/**
 * 事件总线类型
 */
public enum EventBusType {

    /**
     * Redis Pub/Sub
     */
    REDIS,

    /**
     * Apache Kafka
     */
    KAFKA,

    /**
     * Apache RocketMQ
     */
    ROCKETMQ
}

