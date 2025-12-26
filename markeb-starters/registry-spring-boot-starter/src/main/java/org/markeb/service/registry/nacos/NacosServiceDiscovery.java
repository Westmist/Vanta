package org.markeb.service.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.markeb.service.registry.ServiceChangeListener;
import org.markeb.service.registry.ServiceDiscovery;
import org.markeb.service.registry.ServiceInstance;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Nacos 服务发现实现
 */
@Slf4j
public class NacosServiceDiscovery implements ServiceDiscovery {

    private final NamingService namingService;
    private final String group;
    private final Map<String, Map<ServiceChangeListener, EventListener>> listenerMap = new ConcurrentHashMap<>();

    public NacosServiceDiscovery(NamingService namingService, String group) {
        this.namingService = namingService;
        this.group = group;
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceName) {
        try {
            List<Instance> instances = namingService.selectInstances(serviceName, group, true);
            return instances.stream()
                    .map(this::fromNacosInstance)
                    .collect(Collectors.toList());
        } catch (NacosException e) {
            log.error("Failed to get instances from Nacos: {}", serviceName, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getServices() {
        try {
            return namingService.getServicesOfServer(1, Integer.MAX_VALUE, group)
                    .getData();
        } catch (NacosException e) {
            log.error("Failed to get services from Nacos", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void subscribe(String serviceName, ServiceChangeListener listener) {
        EventListener eventListener = event -> {
            if (event instanceof NamingEvent namingEvent) {
                List<ServiceInstance> instances = namingEvent.getInstances().stream()
                        .map(this::fromNacosInstance)
                        .collect(Collectors.toList());
                listener.onServiceChange(serviceName, instances);
            }
        };

        try {
            namingService.subscribe(serviceName, group, eventListener);
            listenerMap.computeIfAbsent(serviceName, k -> new ConcurrentHashMap<>())
                    .put(listener, eventListener);
            log.info("Subscribed to Nacos service: {}", serviceName);
        } catch (NacosException e) {
            log.error("Failed to subscribe to Nacos service: {}", serviceName, e);
            throw new RuntimeException("Failed to subscribe to Nacos service", e);
        }
    }

    @Override
    public void unsubscribe(String serviceName, ServiceChangeListener listener) {
        Map<ServiceChangeListener, EventListener> listeners = listenerMap.get(serviceName);
        if (listeners != null) {
            EventListener eventListener = listeners.remove(listener);
            if (eventListener != null) {
                try {
                    namingService.unsubscribe(serviceName, group, eventListener);
                    log.info("Unsubscribed from Nacos service: {}", serviceName);
                } catch (NacosException e) {
                    log.error("Failed to unsubscribe from Nacos service: {}", serviceName, e);
                }
            }
        }
    }

    @Override
    public void close() {
        try {
            namingService.shutDown();
            listenerMap.clear();
            log.info("Nacos service discovery closed");
        } catch (NacosException e) {
            log.error("Failed to close Nacos service discovery", e);
        }
    }

    private ServiceInstance fromNacosInstance(Instance nacosInstance) {
        return ServiceInstance.builder()
                .instanceId(nacosInstance.getInstanceId())
                .serviceName(nacosInstance.getServiceName())
                .host(nacosInstance.getIp())
                .port(nacosInstance.getPort())
                .weight(nacosInstance.getWeight())
                .healthy(nacosInstance.isHealthy())
                .enabled(nacosInstance.isEnabled())
                .metadata(nacosInstance.getMetadata())
                .build();
    }
}

