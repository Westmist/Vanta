package org.markeb.actor.mailbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 有界邮箱实现
 * <p>
 * 基于 ArrayBlockingQueue 实现的有界邮箱。
 * 当邮箱满时，可以选择丢弃新消息或等待。
 * </p>
 */
public class BoundedMailbox implements Mailbox {

    private static final Logger log = LoggerFactory.getLogger(BoundedMailbox.class);

    private final BlockingQueue<Envelope> queue;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final int capacity;
    private final long offerTimeoutMs;

    /**
     * 创建有界邮箱
     *
     * @param capacity       容量
     * @param offerTimeoutMs 入队超时时间（毫秒），0 表示不等待
     */
    public BoundedMailbox(int capacity, long offerTimeoutMs) {
        this.capacity = capacity;
        this.offerTimeoutMs = offerTimeoutMs;
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    @Override
    public boolean enqueue(Envelope envelope) {
        if (closed.get()) {
            return false;
        }

        try {
            if (offerTimeoutMs <= 0) {
                boolean success = queue.offer(envelope);
                if (!success) {
                    log.warn("Mailbox is full (capacity={}), message dropped: {}",
                            capacity, envelope.getMessage().getClass().getSimpleName());
                }
                return success;
            } else {
                boolean success = queue.offer(envelope, offerTimeoutMs, TimeUnit.MILLISECONDS);
                if (!success) {
                    log.warn("Mailbox offer timeout after {}ms, message dropped: {}",
                            offerTimeoutMs, envelope.getMessage().getClass().getSimpleName());
                }
                return success;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public Envelope dequeue() throws InterruptedException {
        if (closed.get() && queue.isEmpty()) {
            return null;
        }
        return queue.take();
    }

    @Override
    public Envelope tryDequeue() {
        return queue.poll();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public void close() {
        closed.set(true);
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

}

