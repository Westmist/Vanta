package org.markeb.actor.impl;

import org.markeb.actor.*;
import org.markeb.actor.config.ActorProperties;
import org.markeb.actor.executor.ActorExecutor;
import org.markeb.actor.executor.ActorExecutorFactory;
import org.markeb.actor.executor.VirtualThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 默认 Actor 系统实现
 */
public class DefaultActorSystem implements ActorSystem {

    private static final Logger log = LoggerFactory.getLogger(DefaultActorSystem.class);

    private final String name;
    private final ActorProperties properties;
    private final ActorExecutor executor;
    private final Map<String, DefaultActor<?>> actors = new ConcurrentHashMap<>();
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    public DefaultActorSystem(String name, ActorProperties properties) {
        this.name = name;
        this.properties = properties;
        this.executor = ActorExecutorFactory.create(properties);
        log.info("ActorSystem '{}' initialized with executor type: {}", name, properties.getExecutorType());
    }

    public DefaultActorSystem(String name, ActorExecutor executor, ActorProperties properties) {
        this.name = name;
        this.properties = properties;
        this.executor = executor;
        log.info("ActorSystem '{}' initialized with custom executor", name);
    }

    @Override
    public <T> ActorRef spawn(String actorId, T initialState, ActorBehavior<T> behavior) {
        return spawn(actorId, initialState, behavior, ActorConfig.defaults());
    }

    @Override
    public <T> ActorRef spawn(String actorId, T initialState, ActorBehavior<T> behavior, ActorConfig config) {
        if (shutdown.get()) {
            throw new IllegalStateException("ActorSystem is shutdown");
        }

        if (actors.containsKey(actorId)) {
            throw new IllegalArgumentException("Actor with id '" + actorId + "' already exists");
        }

        DefaultActor<T> actor = new DefaultActor<>(actorId, initialState, behavior, config, executor, this);
        actors.put(actorId, actor);
        log.debug("Spawned actor: {}", actorId);
        return actor;
    }

    @Override
    public Optional<ActorRef> lookup(String actorId) {
        DefaultActor<?> actor = actors.get(actorId);
        if (actor != null && !actor.isStopped()) {
            return Optional.of(actor);
        }
        return Optional.empty();
    }

    @Override
    public <T> ActorRef getOrSpawn(String actorId, T initialState, ActorBehavior<T> behavior) {
        return actors.computeIfAbsent(actorId, id -> {
            if (shutdown.get()) {
                throw new IllegalStateException("ActorSystem is shutdown");
            }
            return new DefaultActor<>(id, initialState, behavior, ActorConfig.defaults(), executor, this);
        });
    }

    @Override
    public boolean stop(String actorId) {
        DefaultActor<?> actor = actors.get(actorId);
        if (actor != null) {
            actor.stop();
            return true;
        }
        return false;
    }

    @Override
    public boolean tell(String actorId, Object message) {
        DefaultActor<?> actor = actors.get(actorId);
        if (actor != null && !actor.isStopped()) {
            actor.tell(message);
            return true;
        }
        return false;
    }

    @Override
    public <T> CompletableFuture<T> ask(String actorId, Object message) {
        DefaultActor<?> actor = actors.get(actorId);
        if (actor != null && !actor.isStopped()) {
            return actor.ask(message);
        }
        return CompletableFuture.failedFuture(
                new IllegalArgumentException("Actor not found: " + actorId));
    }

    @Override
    public int actorCount() {
        return (int) actors.values().stream()
                .filter(a -> !a.isStopped())
                .count();
    }

    @Override
    public ExecutorType getExecutorType() {
        return properties.getExecutorType();
    }

    @Override
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            log.info("Shutting down ActorSystem '{}'...", name);

            // 停止所有 Actor
            actors.values().forEach(DefaultActor::stop);
            actors.clear();

            // 关闭执行器
            executor.shutdown();

            log.info("ActorSystem '{}' shutdown complete", name);
        }
    }

    @Override
    public boolean awaitTermination(long timeoutMs) throws InterruptedException {
        return executor.awaitTermination(timeoutMs);
    }

    /**
     * 当 Actor 停止时的回调
     */
    void onActorStopped(String actorId) {
        actors.remove(actorId);
        if (executor instanceof VirtualThreadExecutor vte) {
            vte.cleanupActor(actorId);
        }
        log.debug("Actor removed: {}", actorId);
    }

    /**
     * 调度延迟消息
     */
    void scheduleOnce(String actorId, Object message, long delayMs) {
        executor.schedule(actorId, () -> tell(actorId, message), delayMs);
    }

    /**
     * 调度周期性消息
     */
    String schedulePeriodic(String actorId, Object message, long initialDelayMs, long periodMs) {
        return executor.schedulePeriodic(actorId, () -> tell(actorId, message), initialDelayMs, periodMs);
    }

    /**
     * 取消调度
     */
    void cancelSchedule(String scheduleId) {
        executor.cancelSchedule(scheduleId);
    }

    /**
     * 获取系统名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取执行器
     */
    public ActorExecutor getExecutor() {
        return executor;
    }

}

