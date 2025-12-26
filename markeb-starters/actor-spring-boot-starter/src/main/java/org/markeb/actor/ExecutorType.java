package org.markeb.actor;

/**
 * Actor 执行器类型
 */
public enum ExecutorType {

    /**
     * 虚拟线程执行器
     * <p>
     * 使用 JDK 21+ 的虚拟线程。
     * 每个 Actor 的每条消息都在独立的虚拟线程中处理。
     * 适合 IO 密集型场景，可以创建大量 Actor。
     * </p>
     */
    VIRTUAL,

    /**
     * 平台线程执行器
     * <p>
     * 使用固定大小的平台线程池。
     * 通过 Actor ID 哈希分片，保证同一 Actor 的消息在同一线程处理。
     * 适合 CPU 密集型场景，线程数量有限。
     * </p>
     */
    PLATFORM

}

