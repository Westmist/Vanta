package org.markeb.config;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 配置中心服务接口
 */
public interface ConfigService {

    /**
     * 获取配置
     *
     * @param key 配置键
     * @return 配置值
     */
    String getConfig(String key);

    /**
     * 获取配置（带默认值）
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getConfig(String key, String defaultValue);

    /**
     * 获取配置并转换为指定类型
     *
     * @param key   配置键
     * @param clazz 目标类型
     * @return 配置对象
     */
    <T> T getConfig(String key, Class<T> clazz);

    /**
     * 获取所有配置
     *
     * @param prefix 配置前缀
     * @return 配置Map
     */
    Map<String, String> getConfigs(String prefix);

    /**
     * 设置配置
     *
     * @param key   配置键
     * @param value 配置值
     */
    void setConfig(String key, String value);

    /**
     * 删除配置
     *
     * @param key 配置键
     */
    void removeConfig(String key);

    /**
     * 监听配置变化
     *
     * @param key      配置键
     * @param listener 监听器
     */
    void addListener(String key, Consumer<String> listener);

    /**
     * 移除监听器
     *
     * @param key      配置键
     * @param listener 监听器
     */
    void removeListener(String key, Consumer<String> listener);

    /**
     * 发布配置
     *
     * @param key     配置键
     * @param content 配置内容
     * @return 是否成功
     */
    boolean publishConfig(String key, String content);
}

