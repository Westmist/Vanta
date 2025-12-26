package org.markeb.actor.executor;

import org.markeb.actor.mailbox.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 虚拟线程执行器
 * <p>
 * 使用 JDK 21+ 的虚拟线程来执行 Actor 任务。
 * 每个 Actor 的消息处理在独立的虚拟线程中执行，但通过锁保证同一 Actor 的消息串行处理。
 * </p>
 */
public class VirtualThreadExecutor implements ActorExecutor {

    private static final Logger log = LoggerFactory.getLogger(VirtualThreadExecutor.class);

    private final ExecutorService virtualExecutor;
    private final ScheduledExecutorService scheduler;
    private final Map<Long, Object> actorLocks = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    public VirtualThreadExecutor() {
        // 创建虚拟线程执行器
        this.virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
        // 调度器使用少量平台线程
        this.scheduler = Executors.newScheduledThreadPool(2, Thread.ofPlatform()
                .name("actor-scheduler-", 0)
                .daemon(true)
                .factory());
        log.info("VirtualThreadExecutor initialized");
    }

    @Override
    public void execute(long actorId, Envelope envelope, Runnable task) {
        if (shutdown.get()) {
            log.warn("Executor is shutdown, rejecting task for actor: {}", actorId);
            return;
        }

        virtualExecutor.execute(() -> {
            // 获取或创建 Actor 的锁
            Object lock = actorLocks.computeIfAbsent(actorId, k -> new Object());
            synchronized (lock) {
                try {
                    task.run();
                } catch (Exception e) {
                    log.error("Error executing task for actor: {}", actorId, e);
                    envelope.completeExceptionally(e);
                }
            }
        });
    }

    @Override
    public void schedule(long actorId, Runnable task, long delayMs) {
        if (shutdown.get()) {
            return;
        }

        scheduler.schedule(() -> {
            virtualExecutor.execute(() -> {
                Object lock = actorLocks.computeIfAbsent(actorId, k -> new Object());
                synchronized (lock) {
                    try {
                        task.run();
                    } catch (Exception e) {
                        log.error("Error executing scheduled task for actor: {}", actorId, e);
                    }
                }
            });
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public String schedulePeriodic(long actorId, Runnable task, long initialDelayMs, long periodMs) {
        if (shutdown.get()) {
            return null;
        }

        String scheduleId = UUID.randomUUID().toString();

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            virtualExecutor.execute(() -> {
                Object lock = actorLocks.computeIfAbsent(actorId, k -> new Object());
                synchronized (lock) {
                    try {
                        task.run();
                    } catch (Exception e) {
                        log.error("Error executing periodic task for actor: {}", actorId, e);
                    }
                }
            });
        }, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);

        scheduledTasks.put(scheduleId, future);
        return scheduleId;
    }

    @Override
    public void cancelSchedule(String scheduleId) {
        ScheduledFuture<?> future = scheduledTasks.remove(scheduleId);
        if (future != null) {
            future.cancel(false);
        }
    }

    @Override
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            log.info("Shutting down VirtualThreadExecutor...");

            // 取消所有周期性任务
            scheduledTasks.values().forEach(f -> f.cancel(false));
            scheduledTasks.clear();

            scheduler.shutdown();
            virtualExecutor.shutdown();
        }
    }

    @Override
    public boolean awaitTermination(long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;

        if (!scheduler.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS)) {
            return false;
        }

        long remaining = deadline - System.currentTimeMillis();
        if (remaining <= 0) {
            return false;
        }

        return virtualExecutor.awaitTermination(remaining, TimeUnit.MILLISECONDS);
    }

    /**
     * 清理已停止 Actor 的锁
     */
    public void cleanupActor(long actorId) {
        actorLocks.remove(actorId);
    }

}

