package org.markeb.actor.message;

/**
 * 系统消息
 * <p>
 * 用于 Actor 系统内部的控制消息。
 * </p>
 */
public sealed interface SystemMessage extends ActorMessage {

    /**
     * 启动消息
     */
    record Start() implements SystemMessage {
    }

    /**
     * 停止消息
     */
    record Stop() implements SystemMessage {
    }

    /**
     * 重启消息
     */
    record Restart() implements SystemMessage {
    }

    /**
     * 心跳消息
     */
    record Tick(long timestamp) implements SystemMessage {
        public Tick() {
            this(System.currentTimeMillis());
        }
    }

    /**
     * 定时器消息
     */
    record Timer(String timerId, Object payload) implements SystemMessage {
    }

}

