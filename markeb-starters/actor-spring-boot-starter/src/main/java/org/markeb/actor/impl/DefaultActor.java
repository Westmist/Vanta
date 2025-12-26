package org.markeb.actor.impl;

import org.markeb.actor.*;
import org.markeb.actor.executor.ActorExecutor;
import org.markeb.actor.mailbox.BoundedMailbox;
import org.markeb.actor.mailbox.DefaultMailbox;
import org.markeb.actor.mailbox.Envelope;
import org.markeb.actor.mailbox.Mailbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 默认 Actor 实现
 *
 * @param <T> 状态类型
 */
public class DefaultActor<T> implements Actor, ActorRef {

    private static final Logger log = LoggerFactory.getLogger(DefaultActor.class);

    private final String actorId;
    private final ActorBehavior<T> behavior;
    private final ActorConfig config;
    private final Mailbox mailbox;
    private final ActorExecutor executor;
    private final DefaultActorSystem system;

    private final AtomicReference<T> state;
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final AtomicBoolean processing = new AtomicBoolean(false);

    public DefaultActor(String actorId,
                        T initialState,
                        ActorBehavior<T> behavior,
                        ActorConfig config,
                        ActorExecutor executor,
                        DefaultActorSystem system) {
        this.actorId = actorId;
        this.state = new AtomicReference<>(initialState);
        this.behavior = behavior;
        this.config = config;
        this.executor = executor;
        this.system = system;

        // 根据配置创建邮箱
        if (config.getMailboxCapacity() > 0) {
            this.mailbox = new BoundedMailbox(config.getMailboxCapacity(), config.getMailboxOfferTimeoutMs());
        } else {
            this.mailbox = new DefaultMailbox();
        }

        log.debug("Actor created: {}", actorId);
    }

    @Override
    public String actorId() {
        return actorId;
    }

    @Override
    public void tell(Object message) {
        if (stopped.get()) {
            log.warn("Actor {} is stopped, message dropped: {}", actorId, message.getClass().getSimpleName());
            return;
        }

        Envelope envelope = new Envelope(message, null);
        if (mailbox.enqueue(envelope)) {
            scheduleProcessing();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> CompletableFuture<R> ask(Object message) {
        if (stopped.get()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Actor " + actorId + " is stopped"));
        }

        CompletableFuture<Object> future = new CompletableFuture<>();
        Envelope envelope = new Envelope(message, null, future);

        if (mailbox.enqueue(envelope)) {
            scheduleProcessing();
        } else {
            future.completeExceptionally(new IllegalStateException("Mailbox is full"));
        }

        return (CompletableFuture<R>) future;
    }

    @Override
    public void stop() {
        if (stopped.compareAndSet(false, true)) {
            log.debug("Stopping actor: {}", actorId);
            mailbox.close();
            system.onActorStopped(actorId);
        }
    }

    @Override
    public boolean isStopped() {
        return stopped.get();
    }

    @Override
    public boolean isAlive() {
        return !stopped.get();
    }

    /**
     * 调度消息处理
     */
    private void scheduleProcessing() {
        // 使用 CAS 确保只有一个处理任务在运行
        if (processing.compareAndSet(false, true)) {
            processNextMessage();
        }
    }

    /**
     * 处理下一条消息
     */
    private void processNextMessage() {
        Envelope envelope = mailbox.tryDequeue();

        if (envelope == null) {
            // 邮箱为空，标记处理完成
            processing.set(false);

            // 双重检查：可能在设置 false 后有新消息入队
            if (!mailbox.isEmpty() && processing.compareAndSet(false, true)) {
                processNextMessage();
            }
            return;
        }

        // 提交到执行器处理
        executor.execute(actorId, envelope, () -> {
            try {
                processMessage(envelope);
            } finally {
                // 处理完成后，继续处理下一条消息
                processNextMessage();
            }
        });
    }

    /**
     * 处理单条消息
     */
    private void processMessage(Envelope envelope) {
        if (stopped.get()) {
            envelope.completeExceptionally(new IllegalStateException("Actor is stopped"));
            return;
        }

        Object message = envelope.getMessage();
        ActorContext context = new DefaultActorContext(this, system, envelope);

        try {
            T currentState = state.get();
            T newState = behavior.onMessage(context, currentState, message);
            state.set(newState);

            // 如果是 ask 模式且没有显式回复，自动完成
            if (envelope.isAsk() && !envelope.getFuture().isDone()) {
                envelope.complete(null);
            }

        } catch (Exception e) {
            log.error("Error processing message {} in actor {}", message.getClass().getSimpleName(), actorId, e);

            if (config.isSuperviseExceptions()) {
                envelope.completeExceptionally(e);

                if (!config.isContinueOnException()) {
                    stop();
                }
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 获取当前状态
     */
    public T getState() {
        return state.get();
    }

    /**
     * 获取邮箱大小
     */
    public int getMailboxSize() {
        return mailbox.size();
    }

}

