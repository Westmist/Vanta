package org.markeb.service.registry.etcd;

import org.markeb.service.registry.ServiceChangeListener;
import org.markeb.service.registry.ServiceDiscovery;
import org.markeb.service.registry.ServiceInstance;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Etcd 服务发现实现
 */
@Slf4j
public class EtcdServiceDiscovery implements ServiceDiscovery {

    private static final String SERVICE_PREFIX = "/services/";

    private final Client client;
    private final KV kvClient;
    private final Watch watchClient;
    private final Map<String, Map<ServiceChangeListener, Watch.Watcher>> watcherMap = new ConcurrentHashMap<>();

    public EtcdServiceDiscovery(Client client) {
        this.client = client;
        this.kvClient = client.getKVClient();
        this.watchClient = client.getWatchClient();
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceName) {
        try {
            String prefix = SERVICE_PREFIX + serviceName + "/";
            GetOption option = GetOption.builder()
                    .isPrefix(true)
                    .build();

            GetResponse response = kvClient.get(
                    ByteSequence.from(prefix, StandardCharsets.UTF_8),
                    option
            ).get();

            return response.getKvs().stream()
                    .map(kv -> deserializeInstance(kv.getValue().toString(StandardCharsets.UTF_8)))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get instances from Etcd: {}", serviceName, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getServices() {
        try {
            GetOption option = GetOption.builder()
                    .isPrefix(true)
                    .withKeysOnly(true)
                    .build();

            GetResponse response = kvClient.get(
                    ByteSequence.from(SERVICE_PREFIX, StandardCharsets.UTF_8),
                    option
            ).get();

            return response.getKvs().stream()
                    .map(kv -> kv.getKey().toString(StandardCharsets.UTF_8))
                    .map(key -> key.substring(SERVICE_PREFIX.length()))
                    .map(key -> key.split("/")[0])
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get services from Etcd", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void subscribe(String serviceName, ServiceChangeListener listener) {
        String prefix = SERVICE_PREFIX + serviceName + "/";
        WatchOption option = WatchOption.builder()
                .isPrefix(true)
                .build();

        Watch.Watcher watcher = watchClient.watch(
                ByteSequence.from(prefix, StandardCharsets.UTF_8),
                option,
                response -> {
                    for (WatchEvent event : response.getEvents()) {
                        log.debug("Etcd watch event: {} - {}", event.getEventType(),
                                event.getKeyValue().getKey().toString(StandardCharsets.UTF_8));
                    }
                    // 获取最新的服务实例列表
                    List<ServiceInstance> instances = getInstances(serviceName);
                    listener.onServiceChange(serviceName, instances);
                }
        );

        watcherMap.computeIfAbsent(serviceName, k -> new ConcurrentHashMap<>())
                .put(listener, watcher);
        log.info("Subscribed to Etcd service: {}", serviceName);
    }

    @Override
    public void unsubscribe(String serviceName, ServiceChangeListener listener) {
        Map<ServiceChangeListener, Watch.Watcher> watchers = watcherMap.get(serviceName);
        if (watchers != null) {
            Watch.Watcher watcher = watchers.remove(listener);
            if (watcher != null) {
                watcher.close();
                log.info("Unsubscribed from Etcd service: {}", serviceName);
            }
        }
    }

    @Override
    public void close() {
        watcherMap.values().forEach(watchers -> 
                watchers.values().forEach(Watch.Watcher::close));
        watcherMap.clear();
        client.close();
        log.info("Etcd service discovery closed");
    }

    private ServiceInstance deserializeInstance(String value) {
        String[] parts = value.split("\\|");
        ServiceInstance.ServiceInstanceBuilder builder = ServiceInstance.builder()
                .instanceId(parts[0])
                .serviceName(parts[1])
                .host(parts[2])
                .port(Integer.parseInt(parts[3]))
                .weight(Double.parseDouble(parts[4]))
                .healthy(Boolean.parseBoolean(parts[5]))
                .enabled(Boolean.parseBoolean(parts[6]));

        if (parts.length > 7 && !parts[7].isEmpty()) {
            Map<String, String> metadata = new ConcurrentHashMap<>();
            String[] metaParts = parts[7].split(",");
            for (String metaPart : metaParts) {
                if (metaPart.contains("=")) {
                    String[] kv = metaPart.split("=", 2);
                    metadata.put(kv[0], kv[1]);
                }
            }
            builder.metadata(metadata);
        }

        return builder.build();
    }
}

