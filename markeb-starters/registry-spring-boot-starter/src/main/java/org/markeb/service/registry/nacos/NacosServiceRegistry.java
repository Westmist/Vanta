package org.markeb.service.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.markeb.service.registry.ServiceInstance;
import org.markeb.service.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Nacos 服务注册实现
 */
@Slf4j
public class NacosServiceRegistry implements ServiceRegistry {

    private final NamingService namingService;
    private final String group;

    public NacosServiceRegistry(NamingService namingService, String group) {
        this.namingService = namingService;
        this.group = group;
    }

    @Override
    public void register(ServiceInstance instance) {
        try {
            Instance nacosInstance = toNacosInstance(instance);
            namingService.registerInstance(instance.getServiceName(), group, nacosInstance);
            log.info("Nacos service registered: {} -> {}:{}", instance.getServiceName(), 
                    instance.getHost(), instance.getPort());
        } catch (NacosException e) {
            log.error("Failed to register service to Nacos: {}", instance.getServiceName(), e);
            throw new RuntimeException("Failed to register service to Nacos", e);
        }
    }

    @Override
    public void deregister(ServiceInstance instance) {
        try {
            namingService.deregisterInstance(instance.getServiceName(), group, 
                    instance.getHost(), instance.getPort());
            log.info("Nacos service deregistered: {} -> {}:{}", instance.getServiceName(), 
                    instance.getHost(), instance.getPort());
        } catch (NacosException e) {
            log.error("Failed to deregister service from Nacos: {}", instance.getServiceName(), e);
            throw new RuntimeException("Failed to deregister service from Nacos", e);
        }
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
    public void close() {
        try {
            namingService.shutDown();
            log.info("Nacos naming service closed");
        } catch (NacosException e) {
            log.error("Failed to close Nacos naming service", e);
        }
    }

    private Instance toNacosInstance(ServiceInstance instance) {
        Instance nacosInstance = new Instance();
        nacosInstance.setInstanceId(instance.getInstanceId());
        nacosInstance.setIp(instance.getHost());
        nacosInstance.setPort(instance.getPort());
        nacosInstance.setWeight(instance.getWeight());
        nacosInstance.setHealthy(instance.isHealthy());
        nacosInstance.setEnabled(instance.isEnabled());
        if (instance.getMetadata() != null) {
            nacosInstance.setMetadata(instance.getMetadata());
        }
        return nacosInstance;
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

