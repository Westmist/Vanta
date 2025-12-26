package org.markeb.actor.mailbox;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 默认邮箱实现
 * <p>
 * 基于 LinkedBlockingQueue 实现的无界邮箱。
 * </p>
 */
public class DefaultMailbox implements Mailbox {

    private final BlockingQueue<Envelope> queue;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public DefaultMailbox() {
        this.queue = new LinkedBlockingQueue<>();
    }

    public DefaultMailbox(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    @Override
    public boolean enqueue(Envelope envelope) {
        if (closed.get()) {
            return false;
        }
        return queue.offer(envelope);
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

