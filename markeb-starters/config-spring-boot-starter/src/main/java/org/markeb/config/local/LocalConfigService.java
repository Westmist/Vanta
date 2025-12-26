package org.markeb.config.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.markeb.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 本地配置服务实现（用于测试）
 */
public class LocalConfigService implements ConfigService {

    private static final Logger log = LoggerFactory.getLogger(LocalConfigService.class);

    private final ConcurrentHashMap<String, String> configs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Consumer<String>>> listeners = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getConfig(String key) {
        return configs.get(key);
    }

    @Override
    public String getConfig(String key, String defaultValue) {
        return configs.getOrDefault(key, defaultValue);
    }

    @Override
    public <T> T getConfig(String key, Class<T> clazz) {
        try {
            String value = configs.get(key);
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
        return configs.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void setConfig(String key, String value) {
        String oldValue = configs.put(key, value);
        notifyListeners(key, value);
        log.debug("Set config: {} = {}", key, value);
    }

    @Override
    public void removeConfig(String key) {
        configs.remove(key);
        notifyListeners(key, null);
        log.debug("Removed config: {}", key);
    }

    @Override
    public void addListener(String key, Consumer<String> listener) {
        listeners.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(listener);
        log.debug("Added listener for config: {}", key);
    }

    @Override
    public void removeListener(String key, Consumer<String> listener) {
        List<Consumer<String>> keyListeners = listeners.get(key);
        if (keyListeners != null) {
            keyListeners.remove(listener);
        }
    }

    @Override
    public boolean publishConfig(String key, String content) {
        setConfig(key, content);
        return true;
    }

    private void notifyListeners(String key, String value) {
        List<Consumer<String>> keyListeners = listeners.get(key);
        if (keyListeners != null) {
            for (Consumer<String> listener : keyListeners) {
                try {
                    listener.accept(value);
                } catch (Exception e) {
                    log.error("Error notifying config listener for key: {}", key, e);
                }
            }
        }
    }

    /**
     * 加载配置（用于初始化）
     */
    public void loadConfigs(Map<String, String> initialConfigs) {
        configs.putAll(initialConfigs);
        log.info("Loaded {} configs", initialConfigs.size());
    }
}

