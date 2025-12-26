package org.markeb.service.registry.config;

import com.ecwid.consul.v1.ConsulClient;
import org.markeb.service.registry.ServiceDiscovery;
import org.markeb.service.registry.ServiceRegistry;
import org.markeb.service.registry.consul.ConsulServiceDiscovery;
import org.markeb.service.registry.consul.ConsulServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Consul 自动配置
 */
@Slf4j
@Configuration
@ConditionalOnClass(ConsulClient.class)
@ConditionalOnProperty(prefix = "markeb.registry", name = "type", havingValue = "consul")
@EnableConfigurationProperties(RegistryProperties.class)
public class ConsulAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConsulClient consulClient(RegistryProperties properties) {
        RegistryProperties.ConsulConfig consulConfig = properties.getConsul();

        log.info("Creating Consul Client with host: {}:{}", consulConfig.getHost(), consulConfig.getPort());
        return new ConsulClient(consulConfig.getHost(), consulConfig.getPort());
    }

    @Bean
    @ConditionalOnMissingBean(ServiceRegistry.class)
    public ServiceRegistry consulServiceRegistry(ConsulClient consulClient, RegistryProperties properties) {
        RegistryProperties.ConsulConfig consulConfig = properties.getConsul();
        log.info("Creating Consul ServiceRegistry");
        return new ConsulServiceRegistry(consulClient,
                consulConfig.getHealthCheckInterval(),
                consulConfig.getHealthCheckTimeout());
    }

    @Bean
    @ConditionalOnMissingBean(ServiceDiscovery.class)
    public ServiceDiscovery consulServiceDiscovery(ConsulClient consulClient) {
        log.info("Creating Consul ServiceDiscovery");
        return new ConsulServiceDiscovery(consulClient);
    }
}

