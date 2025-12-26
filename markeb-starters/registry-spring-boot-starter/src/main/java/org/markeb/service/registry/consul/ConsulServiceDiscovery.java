package org.markeb.service.registry.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.HealthService;
import org.markeb.service.registry.ServiceChangeListener;
import org.markeb.service.registry.ServiceDiscovery;
import org.markeb.service.registry.ServiceInstance;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Consul 服务发现实现
 */
@Slf4j
public class ConsulServiceDiscovery implements ServiceDiscovery {

    private final ConsulClient consulClient;
    private final Map<String, List<ServiceChangeListener>> listenerMap = new ConcurrentHashMap<>();
    private final Map<String, Long> indexMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicBoolean> watchingMap = new ConcurrentHashMap<>();
    private final ExecutorService watchExecutor;

    public ConsulServiceDiscovery(ConsulClient consulClient) {
        this.consulClient = consulClient;
        this.watchExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "consul-watch");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceName) {
        try {
            HealthServicesRequest request = HealthServicesRequest.newBuilder()
                    .setPassing(true)
                    .build();
            Response<List<HealthService>> response = consulClient.getHealthServices(serviceName, request);

            if (response.getValue() == null) {
                return Collections.emptyList();
            }

            // 更新索引
            indexMap.put(serviceName, response.getConsulIndex());

            return response.getValue().stream()
                    .map(this::fromConsulService)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get instances from Consul: {}", serviceName, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getServices() {
        try {
            Response<Map<String, List<String>>> response = consulClient.getCatalogServices(QueryParams.DEFAULT);
            if (response.getValue() == null) {
                return Collections.emptyList();
            }
            return new ArrayList<>(response.getValue().keySet());
        } catch (Exception e) {
            log.error("Failed to get services from Consul", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void subscribe(String serviceName, ServiceChangeListener listener) {
        listenerMap.computeIfAbsent(serviceName, k -> new ArrayList<>()).add(listener);

        // 启动长轮询监听
        AtomicBoolean watching = watchingMap.computeIfAbsent(serviceName, k -> new AtomicBoolean(false));
        if (watching.compareAndSet(false, true)) {
            startWatching(serviceName);
        }

        log.info("Subscribed to Consul service: {}", serviceName);
    }

    private void startWatching(String serviceName) {
        watchExecutor.submit(() -> {
            AtomicBoolean watching = watchingMap.get(serviceName);
            while (watching != null && watching.get()) {
                try {
                    Long currentIndex = indexMap.getOrDefault(serviceName, 0L);
                    
                    // 使用阻塞查询（长轮询）
                    QueryParams queryParams = QueryParams.Builder.builder()
                            .setIndex(currentIndex)
                            .setWaitTime(55) // 55秒等待
                            .build();

                    HealthServicesRequest request = HealthServicesRequest.newBuilder()
                            .setPassing(true)
                            .setQueryParams(queryParams)
                            .build();

                    Response<List<HealthService>> response = consulClient.getHealthServices(serviceName, request);

                    Long newIndex = response.getConsulIndex();
                    if (newIndex != null && !newIndex.equals(currentIndex)) {
                        indexMap.put(serviceName, newIndex);

                        List<ServiceInstance> instances = response.getValue() != null
                                ? response.getValue().stream()
                                    .map(this::fromConsulService)
                                    .collect(Collectors.toList())
                                : Collections.emptyList();

                        // 通知监听器
                        List<ServiceChangeListener> listeners = listenerMap.get(serviceName);
                        if (listeners != null) {
                            for (ServiceChangeListener listener : listeners) {
                                try {
                                    listener.onServiceChange(serviceName, instances);
                                } catch (Exception e) {
                                    log.error("Error notifying listener for service: {}", serviceName, e);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    if (watching.get()) {
                        log.error("Error watching Consul service: {}", serviceName, e);
                        try {
                            Thread.sleep(5000); // 发生错误时等待5秒再重试
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void unsubscribe(String serviceName, ServiceChangeListener listener) {
        List<ServiceChangeListener> listeners = listenerMap.get(serviceName);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                listenerMap.remove(serviceName);
                AtomicBoolean watching = watchingMap.remove(serviceName);
                if (watching != null) {
                    watching.set(false);
                }
            }
        }
        log.info("Unsubscribed from Consul service: {}", serviceName);
    }

    @Override
    public void close() {
        watchingMap.values().forEach(watching -> watching.set(false));
        watchingMap.clear();
        listenerMap.clear();
        indexMap.clear();
        watchExecutor.shutdown();
        log.info("Consul service discovery closed");
    }

    private ServiceInstance fromConsulService(HealthService healthService) {
        HealthService.Service service = healthService.getService();
        // 从 meta 中获取权重,如果没有则默认为 1
        int weight = 1;
        if (service.getMeta() != null && service.getMeta().containsKey("weight")) {
            try {
                weight = Integer.parseInt(service.getMeta().get("weight"));
            } catch (NumberFormatException ignored) {
            }
        }
        return ServiceInstance.builder()
                .instanceId(service.getId())
                .serviceName(service.getService())
                .host(service.getAddress())
                .port(service.getPort())
                .weight(weight)
                .healthy(true)
                .enabled(true)
                .metadata(service.getMeta())
                .build();
    }
}

