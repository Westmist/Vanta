package org.markeb.meta.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置表模块配置属性
 */
@Data
@ConfigurationProperties(prefix = "markeb.meta")
public class MetaProperties {

    /**
     * 是否启用配置表模块
     */
    private boolean enabled = true;

    /**
     * 配置文件路径（支持 classpath: 和 file: 前缀）
     * 默认从 classpath:meta/ 加载
     */
    private String path = "classpath:meta/";

    /**
     * 配置文件后缀名
     */
    private String suffix = ".bytes";

    /**
     * 热更新配置
     */
    private HotReload hotReload = new HotReload();

    @Data
    public static class HotReload {

        /**
         * 是否启用热更新
         */
        private boolean enabled = false;

        /**
         * 文件监视防抖延迟（毫秒）
         */
        private long debounceMs = 500;
    }
}

