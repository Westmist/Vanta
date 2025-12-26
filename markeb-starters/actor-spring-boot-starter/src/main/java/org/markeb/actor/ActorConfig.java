package org.markeb.actor;

/**
 * Actor 配置
 */
public class ActorConfig {

    /**
     * 邮箱容量，0 表示无界
     */
    private int mailboxCapacity = 0;

    /**
     * 邮箱入队超时（毫秒），0 表示不等待
     */
    private long mailboxOfferTimeoutMs = 0;

    /**
     * 是否在处理消息时捕获异常
     */
    private boolean superviseExceptions = true;

    /**
     * 异常后是否继续处理后续消息
     */
    private boolean continueOnException = true;

    private ActorConfig() {
    }

    public static ActorConfig defaults() {
        return new ActorConfig();
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getMailboxCapacity() {
        return mailboxCapacity;
    }

    public long getMailboxOfferTimeoutMs() {
        return mailboxOfferTimeoutMs;
    }

    public boolean isSuperviseExceptions() {
        return superviseExceptions;
    }

    public boolean isContinueOnException() {
        return continueOnException;
    }

    public static class Builder {
        private final ActorConfig config = new ActorConfig();

        public Builder mailboxCapacity(int capacity) {
            config.mailboxCapacity = capacity;
            return this;
        }

        public Builder mailboxOfferTimeoutMs(long timeoutMs) {
            config.mailboxOfferTimeoutMs = timeoutMs;
            return this;
        }

        public Builder superviseExceptions(boolean supervise) {
            config.superviseExceptions = supervise;
            return this;
        }

        public Builder continueOnException(boolean continueOn) {
            config.continueOnException = continueOn;
            return this;
        }

        public ActorConfig build() {
            return config;
        }
    }

}

