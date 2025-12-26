package org.markeb.config.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.markeb.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 基于 Nacos 的配置中心实现
 */
public class NacosConfigService implements ConfigService {

    private static final Logger log = LoggerFactory.getLogger(NacosConfigService.class);

    private final com.alibaba.nacos.api.config.ConfigService configService;
    private final ObjectMapper objectMapper;
    private final String group;
    private final long timeout;

    private final ConcurrentHashMap<String, Map<Consumer<String>, Listener>> listeners = new ConcurrentHashMap<>();
    private final Executor listenerExecutor = Executors.newCachedThreadPool();

    public NacosConfigService(String serverAddr, String namespace, String group, long timeout) throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        if (namespace != null && !namespace.isEmpty()) {
            properties.put("namespace", namespace);
        }

        this.configService = NacosFactory.createConfigService(properties);
        this.objectMapper = new ObjectMapper();
        this.group = group;
        this.timeout = timeout;

        log.info("Nacos config service initialized with server: {}", serverAddr);
    }

    @Override
    public String getConfig(String key) {
        try {
            return configService.getConfig(key, group, timeout);
        } catch (NacosException e) {
            log.error("Failed to get config: {}", key, e);
            return null;
        }
    }

    @Override
    public String getConfig(String key, String defaultValue) {
        String value = getConfig(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public <T> T getConfig(String key, Class<T> clazz) {
        try {
            String value = getConfig(key);
            if (value != null) {
                return objectMapper.readValue(value, clazz);
            }
        } catch (Exception e) {
            log.error("Failed to parse config: {}", key, e);
        }
        return null;
    }

    @Override
    public Map<String, String> getConfigs(String prefix) {
        // Nacos 不直接支持前缀查询，返回空 Map
        return new HashMap<>();
    }

    @Override
    public void setConfig(String key, String value) {
        publishConfig(key, value);
    }

    @Override
    public void removeConfig(String key) {
        try {
            configService.removeConfig(key, group);
            log.info("Removed config: {}", key);
        } catch (NacosException e) {
            log.error("Failed to remove config: {}", key, e);
        }
    }

    @Override
    public void addListener(String key, Consumer<String> listener) {
        Listener nacosListener = new Listener() {
            @Override
            public Executor getExecutor() {
                return listenerExecutor;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                listener.accept(configInfo);
            }
        };

        try {
            configService.addListener(key, group, nacosListener);
            listeners.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).put(listener, nacosListener);
            log.debug("Added listener for config: {}", key);
        } catch (NacosException e) {
            log.error("Failed to add listener for config: {}", key, e);
        }
    }

    @Override
    public void removeListener(String key, Consumer<String> listener) {
        Map<Consumer<String>, Listener> keyListeners = listeners.get(key);
        if (keyListeners != null) {
            Listener nacosListener = keyListeners.remove(listener);
            if (nacosListener != null) {
                configService.removeListener(key, group, nacosListener);
                log.debug("Removed listener for config: {}", key);
            }
        }
    }

    @Override
    public boolean publishConfig(String key, String content) {
        try {
            boolean result = configService.publishConfig(key, group, content);
            if (result) {
                log.info("Published config: {}", key);
            }
            return result;
        } catch (NacosException e) {
            log.error("Failed to publish config: {}", key, e);
            return false;
        }
    }
}

