package org.markeb.actor.executor;

import org.markeb.actor.mailbox.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 平台线程执行器
 * <p>
 * 使用固定大小的平台线程池执行 Actor 任务。
 * 通过 Actor ID 哈希分片，保证同一 Actor 的消息始终在同一线程处理，天然串行化。
 * </p>
 */
public class PlatformThreadExecutor implements ActorExecutor {

    private static final Logger log = LoggerFactory.getLogger(PlatformThreadExecutor.class);

    private final ExecutorService[] executors;
    private final int parallelism;
    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    /**
     * 创建平台线程执行器
     *
     * @param parallelism 并行度（线程数）
     */
    public PlatformThreadExecutor(int parallelism) {
        this.parallelism = parallelism;
        this.executors = new ExecutorService[parallelism];

        // 为每个分片创建单线程执行器
        for (int i = 0; i < parallelism; i++) {
            final int index = i;
            ThreadFactory factory = new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("actor-worker-" + index + "-" + counter.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            };
            // 单线程执行器保证同一分片内的任务串行执行
            this.executors[i] = Executors.newSingleThreadExecutor(factory);
        }

        // 调度器
        this.scheduler = Executors.newScheduledThreadPool(2, Thread.ofPlatform()
                .name("actor-scheduler-", 0)
                .daemon(true)
                .factory());

        log.info("PlatformThreadExecutor initialized with {} workers", parallelism);
    }

    /**
     * 根据 Actor ID 计算分片索引
     */
    private int getShardIndex(String actorId) {
        int hash = actorId.hashCode();
        // 确保非负
        return (hash & 0x7FFFFFFF) % parallelism;
    }

    @Override
    public void execute(String actorId, Envelope envelope, Runnable task) {
        if (shutdown.get()) {
            log.warn("Executor is shutdown, rejecting task for actor: {}", actorId);
            return;
        }

        int shardIndex = getShardIndex(actorId);
        executors[shardIndex].execute(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("Error executing task for actor: {} on shard: {}", actorId, shardIndex, e);
                envelope.completeExceptionally(e);
            }
        });
    }

    @Override
    public void schedule(String actorId, Runnable task, long delayMs) {
        if (shutdown.get()) {
            return;
        }

        int shardIndex = getShardIndex(actorId);
        scheduler.schedule(() -> {
            executors[shardIndex].execute(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    log.error("Error executing scheduled task for actor: {}", actorId, e);
                }
            });
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public String schedulePeriodic(String actorId, Runnable task, long initialDelayMs, long periodMs) {
        if (shutdown.get()) {
            return null;
        }

        String scheduleId = UUID.randomUUID().toString();
        int shardIndex = getShardIndex(actorId);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            executors[shardIndex].execute(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    log.error("Error executing periodic task for actor: {}", actorId, e);
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
            log.info("Shutting down PlatformThreadExecutor...");

            // 取消所有周期性任务
            scheduledTasks.values().forEach(f -> f.cancel(false));
            scheduledTasks.clear();

            scheduler.shutdown();
            for (ExecutorService executor : executors) {
                executor.shutdown();
            }
        }
    }

    @Override
    public boolean awaitTermination(long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;

        if (!scheduler.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS)) {
            return false;
        }

        for (ExecutorService executor : executors) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                return false;
            }
            if (!executor.awaitTermination(remaining, TimeUnit.MILLISECONDS)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取并行度
     */
    public int getParallelism() {
        return parallelism;
    }

}

