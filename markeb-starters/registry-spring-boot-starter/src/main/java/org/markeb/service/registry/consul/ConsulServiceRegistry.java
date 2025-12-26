package org.markeb.service.registry.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.HealthService;
import org.markeb.service.registry.ServiceInstance;
import org.markeb.service.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Consul 服务注册实现
 */
@Slf4j
public class ConsulServiceRegistry implements ServiceRegistry {

    private final ConsulClient consulClient;
    private final String healthCheckInterval;
    private final String healthCheckTimeout;

    public ConsulServiceRegistry(ConsulClient consulClient, String healthCheckInterval, String healthCheckTimeout) {
        this.consulClient = consulClient;
        this.healthCheckInterval = healthCheckInterval;
        this.healthCheckTimeout = healthCheckTimeout;
    }

    public ConsulServiceRegistry(ConsulClient consulClient) {
        this(consulClient, "10s", "5s");
    }

    @Override
    public void register(ServiceInstance instance) {
        try {
            NewService newService = new NewService();
            newService.setId(instance.getInstanceId());
            newService.setName(instance.getServiceName());
            newService.setAddress(instance.getHost());
            newService.setPort(instance.getPort());
            
            // 设置 meta,包含权重信息
            Map<String, String> meta = instance.getMetadata() != null 
                    ? new java.util.HashMap<>(instance.getMetadata()) 
                    : new java.util.HashMap<>();
            meta.put("weight", String.valueOf((int) instance.getWeight()));
            newService.setMeta(meta);

            // 设置健康检查
            NewService.Check check = new NewService.Check();
            check.setTcp(instance.getHost() + ":" + instance.getPort());
            check.setInterval(healthCheckInterval);
            check.setTimeout(healthCheckTimeout);
            check.setDeregisterCriticalServiceAfter("30s");
            newService.setCheck(check);

            consulClient.agentServiceRegister(newService);
            log.info("Consul service registered: {} -> {}:{}", instance.getServiceName(),
                    instance.getHost(), instance.getPort());
        } catch (Exception e) {
            log.error("Failed to register service to Consul: {}", instance.getServiceName(), e);
            throw new RuntimeException("Failed to register service to Consul", e);
        }
    }

    @Override
    public void deregister(ServiceInstance instance) {
        try {
            consulClient.agentServiceDeregister(instance.getInstanceId());
            log.info("Consul service deregistered: {} -> {}:{}", instance.getServiceName(),
                    instance.getHost(), instance.getPort());
        } catch (Exception e) {
            log.error("Failed to deregister service from Consul: {}", instance.getServiceName(), e);
            throw new RuntimeException("Failed to deregister service from Consul", e);
        }
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
            Response<Map<String, List<String>>> response = consulClient.getCatalogServices(com.ecwid.consul.v1.QueryParams.DEFAULT);
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
    public void close() {
        // ConsulClient doesn't have a close method
        log.info("Consul service registry closed");
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
                .healthy(true) // 只返回健康的服务
                .enabled(true)
                .metadata(service.getMeta())
                .build();
    }
}

