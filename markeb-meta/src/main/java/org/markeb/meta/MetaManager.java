package org.markeb.meta;

import org.markeb.meta.luban.ByteBuf;
import org.markeb.meta.luban.IBean;
import org.markeb.meta.luban.ITable;
import org.markeb.meta.luban.Tables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * 配置表管理器
 * 负责配置表的加载、访问和热更新
 * 
 * 使用双缓冲机制实现无锁读取和安全热更新：
 * - 读取操作：直接访问当前 Tables，无需加锁
 * - 热更新：在新的 Tables 中加载数据，加载完成后原子替换
 */
@Slf4j
public class MetaManager implements TableRegistry {

    private final AtomicReference<Tables> tablesRef = new AtomicReference<>(new Tables());
    private final Map<Class<?>, TableInfo<?, ?, ?>> tableInfoMap = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock reloadLock = new ReentrantReadWriteLock();

    private TableLoader tableLoader;
    private ApplicationEventPublisher eventPublisher;

    /**
     * 设置表数据加载器
     *
     * @param tableLoader 加载器
     */
    public void setTableLoader(TableLoader tableLoader) {
        this.tableLoader = tableLoader;
    }

    /**
     * 设置事件发布器
     *
     * @param eventPublisher 事件发布器
     */
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public <K, V extends IBean, T extends ITable<K, V>> void register(Class<T> tableClass, Supplier<T> factory) {
        T instance = factory.get();
        tableInfoMap.put(tableClass, new TableInfo<>(tableClass, factory, instance.getTableName()));
        log.debug("Registered table: {} -> {}", tableClass.getSimpleName(), instance.getTableName());
    }

    /**
     * 获取配置表
     *
     * @param tableClass 表类型
     * @param <T>        表类型
     * @return 表实例
     */
    public <T extends ITable<?, ?>> T getTable(Class<T> tableClass) {
        return tablesRef.get().get(tableClass);
    }

    /**
     * 获取配置表（不存在则抛异常）
     *
     * @param tableClass 表类型
     * @param <T>        表类型
     * @return 表实例
     */
    public <T extends ITable<?, ?>> T getTableOrThrow(Class<T> tableClass) {
        return tablesRef.get().getOrThrow(tableClass);
    }

    /**
     * 加载所有配置表
     *
     * @throws IOException 加载失败时抛出
     */
    public void loadAll() throws IOException {
        reloadLock.writeLock().lock();
        try {
            Tables newTables = new Tables();
            
            for (TableInfo<?, ?, ?> info : tableInfoMap.values()) {
                loadTable(newTables, info);
            }
            
            // 解析引用关系
            newTables.resolveAll();
            
            // 原子替换
            tablesRef.set(newTables);
            
            log.info("Loaded {} tables successfully", tableInfoMap.size());
        } finally {
            reloadLock.writeLock().unlock();
        }
    }

    /**
     * 热更新所有配置表
     *
     * @throws IOException 加载失败时抛出
     */
    public void reloadAll() throws IOException {
        loadAll();
        publishReloadEvent(new MetaReloadEvent(this));
    }

    /**
     * 热更新指定配置表
     *
     * @param tableClasses 要重载的表类型
     * @throws IOException 加载失败时抛出
     */
    @SafeVarargs
    public final void reload(Class<? extends ITable<?, ?>>... tableClasses) throws IOException {
        if (tableClasses == null || tableClasses.length == 0) {
            return;
        }

        reloadLock.writeLock().lock();
        try {
            // 创建新的 Tables，复制现有数据
            Tables oldTables = tablesRef.get();
            Tables newTables = new Tables();
            
            Set<String> reloadedTableNames = new HashSet<>();
            Set<Class<?>> reloadClasses = new HashSet<>(Arrays.asList(tableClasses));
            
            for (TableInfo<?, ?, ?> info : tableInfoMap.values()) {
                if (reloadClasses.contains(info.tableClass)) {
                    // 重新加载指定的表
                    loadTable(newTables, info);
                    reloadedTableNames.add(info.tableName);
                } else {
                    // 复制现有的表
                    copyTable(newTables, oldTables, info);
                }
            }
            
            // 解析引用关系
            newTables.resolveAll();
            
            // 原子替换
            tablesRef.set(newTables);
            
            log.info("Reloaded {} tables: {}", reloadedTableNames.size(), reloadedTableNames);
            publishReloadEvent(new MetaReloadEvent(this, reloadedTableNames));
        } finally {
            reloadLock.writeLock().unlock();
        }
    }

    /**
     * 根据表名热更新配置表
     *
     * @param tableNames 要重载的表名
     * @throws IOException 加载失败时抛出
     */
    public void reloadByName(String... tableNames) throws IOException {
        if (tableNames == null || tableNames.length == 0) {
            return;
        }

        Set<String> nameSet = new HashSet<>(Arrays.asList(tableNames));
        List<Class<? extends ITable<?, ?>>> classes = new ArrayList<>();
        
        for (TableInfo<?, ?, ?> info : tableInfoMap.values()) {
            if (nameSet.contains(info.tableName)) {
                @SuppressWarnings("unchecked")
                Class<? extends ITable<?, ?>> clazz = (Class<? extends ITable<?, ?>>) info.tableClass;
                classes.add(clazz);
            }
        }
        
        if (!classes.isEmpty()) {
            @SuppressWarnings("unchecked")
            Class<? extends ITable<?, ?>>[] array = classes.toArray(new Class[0]);
            reload(array);
        }
    }

    @SuppressWarnings("unchecked")
    private <K, V extends IBean, T extends ITable<K, V>> void loadTable(Tables tables, TableInfo<K, V, T> info) throws IOException {
        T table = info.factory.get();
        ByteBuf buf = tableLoader.load(info.tableName);
        table.load(buf);
        tables.register(info.tableClass, table);
        log.debug("Loaded table: {} ({} records)", info.tableName, table.size());
    }

    @SuppressWarnings("unchecked")
    private <K, V extends IBean, T extends ITable<K, V>> void copyTable(Tables newTables, Tables oldTables, TableInfo<K, V, T> info) {
        T existingTable = oldTables.get(info.tableClass);
        if (existingTable != null) {
            newTables.register(info.tableClass, existingTable);
        }
    }

    private void publishReloadEvent(MetaReloadEvent event) {
        if (eventPublisher != null) {
            try {
                eventPublisher.publishEvent(event);
            } catch (Exception e) {
                log.error("Failed to publish reload event", e);
            }
        }
    }

    /**
     * 获取所有已注册的表名
     *
     * @return 表名集合
     */
    public Set<String> getTableNames() {
        Set<String> names = new HashSet<>();
        for (TableInfo<?, ?, ?> info : tableInfoMap.values()) {
            names.add(info.tableName);
        }
        return names;
    }

    /**
     * 表信息记录
     */
    private record TableInfo<K, V extends IBean, T extends ITable<K, V>>(
            Class<T> tableClass,
            Supplier<T> factory,
            String tableName
    ) {
    }
}

