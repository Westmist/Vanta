package org.markeb.persistent.queue.memory;

import org.markeb.persistent.queue.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 内存持久化队列实现
 * 用于单机测试或开发环境
 */
public class MemoryPersistentQueue implements PersistentQueue {

    private static final Logger log = LoggerFactory.getLogger(MemoryPersistentQueue.class);

    private final BlockingQueue<PersistentMessage> queue;
    private final ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private PersistentMessageHandler handler;

    public MemoryPersistentQueue() {
        this(1000); // 默认队列大小
    }

    public MemoryPersistentQueue(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "memory-persistent-queue");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public QueueType getType() {
        return QueueType.MEMORY;
    }

    @Override
    public CompletableFuture<Void> sendAsync(PersistentMessage message) {
        return CompletableFuture.runAsync(() -> {
            try {
                boolean offered = queue.offer(message, 5, TimeUnit.SECONDS);
                if (!offered) {
                    throw new RuntimeException("Queue is full");
                }
                log.debug("Sent persistent message to memory queue: {} -> {}",
                        message.getEntityClass(), message.getEntityId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while sending message", e);
            }
        });
    }

    @Override
    public void sendSync(PersistentMessage message) {
        try {
            boolean offered = queue.offer(message, 5, TimeUnit.SECONDS);
            if (!offered) {
                throw new RuntimeException("Queue is full");
            }
            log.debug("Sent persistent message sync to memory queue: {} -> {}",
                    message.getEntityClass(), message.getEntityId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while sending message", e);
        }
    }

    @Override
    public void subscribe(PersistentMessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            executor.submit(this::consumeLoop);
            log.info("Memory persistent queue started");
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
            log.info("Memory persistent queue stopped");
        }
    }

    private void consumeLoop() {
        while (running.get()) {
            try {
                PersistentMessage message = queue.poll(1, TimeUnit.SECONDS);
                if (message != null && handler != null) {
                    try {
                        handler.handle(message);
                    } catch (Exception e) {
                        log.error("Failed to handle persistent message: {} -> {}",
                                message.getEntityClass(), message.getEntityId(), e);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * 获取队列中待处理的消息数量
     */
    public int getPendingCount() {
        return queue.size();
    }
}
