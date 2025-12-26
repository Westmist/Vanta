package org.markeb.id.worker;

/**
 * WorkerId 分配器接口
 */
@FunctionalInterface
public interface WorkerIdAssigner {

    /**
     * 分配一个 WorkerId
     *
     * @return WorkerId
     */
    int assignWorkerId();
}

