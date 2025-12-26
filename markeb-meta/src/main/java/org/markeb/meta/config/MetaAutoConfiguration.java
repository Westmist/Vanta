package org.markeb.meta.config;

import org.markeb.meta.MetaManager;
import org.markeb.meta.TableLoader;
import org.markeb.meta.TableRegistrar;
import org.markeb.meta.loader.ClasspathTableLoader;
import org.markeb.meta.loader.FileTableLoader;
import org.markeb.meta.watcher.FileWatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 配置表模块自动配置
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MetaProperties.class)
@ConditionalOnProperty(prefix = "markeb.meta", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MetaAutoConfiguration {

    private final MetaProperties properties;
    private final ApplicationEventPublisher eventPublisher;
    private final List<TableRegistrar> registrars;

    private FileWatcher fileWatcher;

    @Autowired
    public MetaAutoConfiguration(MetaProperties properties,
                                  ApplicationEventPublisher eventPublisher,
                                  @Autowired(required = false) List<TableRegistrar> registrars) {
        this.properties = properties;
        this.eventPublisher = eventPublisher;
        this.registrars = registrars != null ? registrars : List.of();
    }

    @Bean
    @ConditionalOnMissingBean
    public TableLoader tableLoader() {
        String path = properties.getPath();
        String suffix = properties.getSuffix();

        if (path.startsWith("classpath:")) {
            String classpathPath = path.substring("classpath:".length());
            log.info("Using ClasspathTableLoader with path: {}", classpathPath);
            return new ClasspathTableLoader(classpathPath, suffix);
        } else if (path.startsWith("file:")) {
            String filePath = path.substring("file:".length());
            log.info("Using FileTableLoader with path: {}", filePath);
            return new FileTableLoader(filePath, suffix);
        } else {
            // 默认作为文件路径处理
            log.info("Using FileTableLoader with path: {}", path);
            return new FileTableLoader(path, suffix);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public MetaManager metaManager(TableLoader tableLoader) {
        MetaManager manager = new MetaManager();
        manager.setTableLoader(tableLoader);
        manager.setEventPublisher(eventPublisher);

        // 注册所有配置表
        for (TableRegistrar registrar : registrars) {
            registrar.register(manager);
            log.debug("Registered tables from: {}", registrar.getClass().getName());
        }

        return manager;
    }

    @PostConstruct
    public void init() throws IOException {
        // 延迟到 Bean 创建完成后再加载
    }

    @Bean
    public MetaInitializer metaInitializer(MetaManager metaManager, TableLoader tableLoader) {
        return new MetaInitializer(metaManager, tableLoader, properties, this::startFileWatcher);
    }

    private void startFileWatcher(MetaManager metaManager, TableLoader tableLoader) {
        if (!properties.getHotReload().isEnabled()) {
            return;
        }

        String path = properties.getPath();
        Path watchPath;

        if (path.startsWith("file:")) {
            watchPath = Paths.get(path.substring("file:".length()));
        } else if (!path.startsWith("classpath:")) {
            watchPath = Paths.get(path);
        } else {
            log.warn("Hot reload is not supported for classpath resources. Please use file: prefix.");
            return;
        }

        try {
            fileWatcher = new FileWatcher(
                    watchPath,
                    metaManager,
                    properties.getSuffix(),
                    properties.getHotReload().getDebounceMs()
            );
            fileWatcher.start();
        } catch (IOException e) {
            log.error("Failed to start file watcher", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (fileWatcher != null) {
            fileWatcher.stop();
        }
    }

    /**
     * 配置表初始化器
     * 在 Spring 容器完全启动后加载配置表
     */
    @Slf4j
    public static class MetaInitializer {

        private final MetaManager metaManager;
        private final TableLoader tableLoader;
        private final MetaProperties properties;
        private final FileWatcherStarter fileWatcherStarter;

        public MetaInitializer(MetaManager metaManager,
                               TableLoader tableLoader,
                               MetaProperties properties,
                               FileWatcherStarter fileWatcherStarter) {
            this.metaManager = metaManager;
            this.tableLoader = tableLoader;
            this.properties = properties;
            this.fileWatcherStarter = fileWatcherStarter;
        }

        @PostConstruct
        public void init() {
            try {
                log.info("Loading meta tables from: {}", properties.getPath());
                metaManager.loadAll();
                log.info("Meta tables loaded successfully");

                // 启动文件监视器
                fileWatcherStarter.start(metaManager, tableLoader);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load meta tables", e);
            }
        }
    }

    @FunctionalInterface
    interface FileWatcherStarter {
        void start(MetaManager metaManager, TableLoader tableLoader);
    }
}

