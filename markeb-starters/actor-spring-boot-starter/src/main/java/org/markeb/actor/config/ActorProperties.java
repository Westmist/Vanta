package org.markeb.actor.config;

import org.markeb.actor.ExecutorType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Actor 模块配置属性
 * <p>
 * 遵循约定大于配置原则，所有配置都有合理的默认值，开箱即用。
 * 用户只需覆盖需要自定义的配置项。
 * </p>
 *
 * <h3>默认约定：</h3>
 * <ul>
 *   <li>默认启用 Actor 模块</li>
 *   <li>默认使用虚拟线程（JDK 21+，更适合游戏服务器的 IO 密集场景）</li>
 *   <li>系统名称默认从 spring.application.name 获取</li>
 *   <li>邮箱默认无界（游戏场景下消息量可控）</li>
 * </ul>
 *
 * <h3>最小配置示例（零配置即可使用）：</h3>
 * <pre>
 * # 无需任何配置，直接使用默认值
 * </pre>
 *
 * <h3>自定义配置示例：</h3>
 * <pre>
 * markeb:
 *   actor:
 *     executor-type: PLATFORM  # 切换为平台线程
 *     parallelism: 8           # 指定线程数
 * </pre>
 */
@ConfigurationProperties(prefix = "markeb.actor")
public class ActorProperties {

    /**
     * 是否启用 Actor 模块
     * <p>默认：true</p>
     */
    private boolean enabled = true;

    /**
     * Actor 系统名称
     * <p>默认：null（将自动使用 spring.application.name，如果未设置则使用 "markeb"）</p>
     */
    private String systemName;

    /**
     * 执行器类型
     * <p>默认：VIRTUAL（虚拟线程，推荐用于 IO 密集型的游戏服务器）</p>
     * <ul>
     *   <li>VIRTUAL - 虚拟线程，轻量级，可支持百万级 Actor</li>
     *   <li>PLATFORM - 平台线程，适合 CPU 密集型场景</li>
     * </ul>
     */
    private ExecutorType executorType = ExecutorType.VIRTUAL;

    /**
     * 平台线程执行器的并行度（线程数）
     * <p>默认：0（自动使用 CPU 核心数）</p>
     * <p>仅当 executorType = PLATFORM 时生效</p>
     */
    private int parallelism = 0;

    /**
     * 默认邮箱容量
     * <p>默认：0（无界邮箱）</p>
     * <p>设置为正数可限制邮箱大小，防止消息堆积</p>
     */
    private int defaultMailboxCapacity = 0;

    /**
     * 默认邮箱入队超时（毫秒）
     * <p>默认：0（不等待，立即返回）</p>
     * <p>仅当 defaultMailboxCapacity > 0 时生效</p>
     */
    private long defaultMailboxOfferTimeoutMs = 0;

    /**
     * 关闭时等待超时（毫秒）
     * <p>默认：30000（30秒）</p>
     */
    private long shutdownTimeoutMs = 30000;

    // ==================== Getters & Setters ====================

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public ExecutorType getExecutorType() {
        return executorType;
    }

    public void setExecutorType(ExecutorType executorType) {
        this.executorType = executorType;
    }

    public int getParallelism() {
        return parallelism;
    }

    public void setParallelism(int parallelism) {
        this.parallelism = parallelism;
    }

    public int getDefaultMailboxCapacity() {
        return defaultMailboxCapacity;
    }

    public void setDefaultMailboxCapacity(int defaultMailboxCapacity) {
        this.defaultMailboxCapacity = defaultMailboxCapacity;
    }

    public long getDefaultMailboxOfferTimeoutMs() {
        return defaultMailboxOfferTimeoutMs;
    }

    public void setDefaultMailboxOfferTimeoutMs(long defaultMailboxOfferTimeoutMs) {
        this.defaultMailboxOfferTimeoutMs = defaultMailboxOfferTimeoutMs;
    }

    public long getShutdownTimeoutMs() {
        return shutdownTimeoutMs;
    }

    public void setShutdownTimeoutMs(long shutdownTimeoutMs) {
        this.shutdownTimeoutMs = shutdownTimeoutMs;
    }

    /**
     * 获取有效的系统名称
     * <p>如果未配置，返回默认值 "markeb"</p>
     *
     * @param applicationName Spring 应用名称（可为 null）
     * @return 系统名称
     */
    public String resolveSystemName(String applicationName) {
        if (systemName != null && !systemName.isBlank()) {
            return systemName;
        }
        if (applicationName != null && !applicationName.isBlank()) {
            return applicationName;
        }
        return "markeb";
    }

    /**
     * 获取有效的并行度
     *
     * @return 并行度，如果配置为 0 则返回 CPU 核心数
     */
    public int resolveParallelism() {
        return parallelism > 0 ? parallelism : Runtime.getRuntime().availableProcessors();
    }

}
