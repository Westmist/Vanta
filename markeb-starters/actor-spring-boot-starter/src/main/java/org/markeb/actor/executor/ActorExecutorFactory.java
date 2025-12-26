package org.markeb.actor.executor;

import org.markeb.actor.ExecutorType;
import org.markeb.actor.config.ActorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Actor 执行器工厂
 * <p>
 * 根据配置创建相应类型的执行器。
 * </p>
 */
public class ActorExecutorFactory {

    private static final Logger log = LoggerFactory.getLogger(ActorExecutorFactory.class);

    /**
     * 创建执行器
     *
     * @param properties 配置
     * @return 执行器实例
     */
    public static ActorExecutor create(ActorProperties properties) {
        ExecutorType type = properties.getExecutorType();

        return switch (type) {
            case VIRTUAL -> {
                log.debug("Creating VirtualThreadExecutor");
                yield new VirtualThreadExecutor();
            }
            case PLATFORM -> {
                int parallelism = properties.resolveParallelism();
                log.debug("Creating PlatformThreadExecutor with parallelism: {}", parallelism);
                yield new PlatformThreadExecutor(parallelism);
            }
        };
    }

    /**
     * 创建虚拟线程执行器
     */
    public static ActorExecutor createVirtual() {
        return new VirtualThreadExecutor();
    }

    /**
     * 创建平台线程执行器
     *
     * @param parallelism 并行度
     */
    public static ActorExecutor createPlatform(int parallelism) {
        return new PlatformThreadExecutor(parallelism);
    }

}

