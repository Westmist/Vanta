package org.markeb.meta.watcher;

import org.markeb.meta.MetaManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 配置文件监视器
 * 监视配置文件变化，自动触发热更新
 */
@Slf4j
public class FileWatcher implements AutoCloseable {

    private final Path watchPath;
    private final MetaManager metaManager;
    private final String suffix;
    private final long debounceMs;

    private WatchService watchService;
    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 创建文件监视器
     *
     * @param watchPath   监视路径
     * @param metaManager 配置管理器
     */
    public FileWatcher(Path watchPath, MetaManager metaManager) {
        this(watchPath, metaManager, ".bytes", 500);
    }

    /**
     * 创建文件监视器
     *
     * @param watchPath   监视路径
     * @param metaManager 配置管理器
     * @param suffix      配置文件后缀
     * @param debounceMs  防抖延迟（毫秒）
     */
    public FileWatcher(Path watchPath, MetaManager metaManager, String suffix, long debounceMs) {
        this.watchPath = watchPath;
        this.metaManager = metaManager;
        this.suffix = suffix;
        this.debounceMs = debounceMs;
    }

    /**
     * 启动文件监视
     *
     * @throws IOException 启动失败时抛出
     */
    public void start() throws IOException {
        if (running.getAndSet(true)) {
            log.warn("FileWatcher is already running");
            return;
        }

        watchService = FileSystems.getDefault().newWatchService();
        watchPath.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY);

        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "meta-file-watcher");
            t.setDaemon(true);
            return t;
        });

        executor.submit(this::watchLoop);
        log.info("FileWatcher started, watching: {}", watchPath);
    }

    /**
     * 停止文件监视
     */
    public void stop() {
        if (!running.getAndSet(false)) {
            return;
        }

        if (executor != null) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                log.error("Failed to close watch service", e);
            }
        }

        log.info("FileWatcher stopped");
    }

    @Override
    public void close() {
        stop();
    }

    private void watchLoop() {
        Set<String> pendingReloads = new HashSet<>();
        long lastEventTime = 0;

        while (running.get()) {
            try {
                WatchKey key = watchService.poll(100, TimeUnit.MILLISECONDS);

                if (key != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        }

                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                        Path fileName = pathEvent.context();
                        String name = fileName.toString();

                        if (name.endsWith(suffix)) {
                            String tableName = name.substring(0, name.length() - suffix.length());
                            if (metaManager.getTableNames().contains(tableName)) {
                                pendingReloads.add(tableName);
                                lastEventTime = System.currentTimeMillis();
                                log.debug("Detected change: {}", tableName);
                            }
                        }
                    }
                    key.reset();
                }

                // 防抖处理：等待一段时间没有新事件后才触发重载
                if (!pendingReloads.isEmpty() && 
                    System.currentTimeMillis() - lastEventTime >= debounceMs) {
                    
                    String[] tables = pendingReloads.toArray(new String[0]);
                    pendingReloads.clear();
                    
                    try {
                        log.info("Reloading tables: {}", (Object) tables);
                        metaManager.reloadByName(tables);
                    } catch (Exception e) {
                        log.error("Failed to reload tables", e);
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (ClosedWatchServiceException e) {
                break;
            } catch (Exception e) {
                log.error("Error in watch loop", e);
            }
        }
    }

    /**
     * 是否正在运行
     *
     * @return 是否运行中
     */
    public boolean isRunning() {
        return running.get();
    }
}

