package org.markeb;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Actor 模型示例：可切换传统线程池或虚拟线程池，邮箱非阻塞，顺序执行
 */
public class ActorTest {

    static class Player {
        private final String id;
        private final String name;
        public final AtomicInteger taskId = new AtomicInteger(1);
        public final AtomicBoolean running = new AtomicBoolean(false);
        private final ConcurrentLinkedQueue<ActorTask> mailbox = new ConcurrentLinkedQueue<>();
        private Executor executor;

        Player(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setExecutor(Executor executor) {
            this.executor = executor;
        }

        public void submit(ActorTask task) {
            mailbox.offer(task);
            tryStart();
        }

        private void tryStart() {
            if (running.compareAndSet(false, true)) {
                executor.execute(this::runLoop);
            }
        }

        private void runLoop() {
            for (; ; ) {
                ActorTask task = mailbox.poll();
                if (task == null) {
                    running.set(false);
                    if (!mailbox.isEmpty() && running.compareAndSet(false, true)) {
                        continue;
                    }
                    break;
                }
                try {
                    // 设置虚拟线程名字
                    if (Thread.currentThread().isVirtual()) {
                        Thread.currentThread().setName("Virtual-ActorPool-" + name + "-id-" + System.nanoTime());
                    }
                    task.run();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        boolean useVirtual = true;

        Executor executor;
        if (useVirtual) {
            // 虚拟线程池，带前缀
            executor = Executors.newVirtualThreadPerTaskExecutor();
            System.out.println("Using Virtual Thread Executor");
        } else {
            executor = new ActorExecutorPool(4);
            System.out.println("Using Platform ThreadPoolExecutor");
        }

        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Player player = new Player(String.valueOf(i), "Player-" + i);
            player.setExecutor(executor);
            players.add(player);
        }

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleWithFixedDelay(() -> {
            for (Player player : players) {
                ActorTask task = createTask(player);
                player.submit(task);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    public static ActorTask createTask(Player player) {
        int taskId = player.taskId.getAndIncrement();
        return new ActorTask(player, () -> {
            System.out.println(Thread.currentThread().getName() + " - Executing task: " + taskId + " for " + player.getName());
            int sleepTime = 100;
            if (taskId == 5 && "1".equals(player.id)) {
                sleepTime = 3000000;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    static class ActorTask implements Runnable {
        private final Player player;
        private final Runnable runnable;

        ActorTask(Player player, Runnable runnable) {
            this.player = player;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            runnable.run();
        }
    }


    static class ActorExecutorPool extends ThreadPoolExecutor {
        public static AtomicInteger threadNumber = new AtomicInteger(0);

        public ActorExecutorPool(int nThreads) {
            super(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
                Thread t = new Thread(r);
                t.setName("ActorPool-Thread-" + threadNumber.getAndIncrement());
                return t;
            });
        }
    }
}
