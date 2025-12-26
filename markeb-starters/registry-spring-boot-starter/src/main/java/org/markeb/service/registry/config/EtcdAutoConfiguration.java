package org.markeb.service.registry.config;

import org.markeb.service.registry.ServiceDiscovery;
import org.markeb.service.registry.ServiceRegistry;
import org.markeb.service.registry.etcd.EtcdServiceDiscovery;
import org.markeb.service.registry.etcd.EtcdServiceRegistry;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Etcd 自动配置
 */
@Slf4j
@Configuration
@ConditionalOnClass(Client.class)
@ConditionalOnProperty(prefix = "markeb.registry", name = "type", havingValue = "etcd")
@EnableConfigurationProperties(RegistryProperties.class)
public class EtcdAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Client etcdClient(RegistryProperties properties) {
        RegistryProperties.EtcdConfig etcdConfig = properties.getEtcd();

        ClientBuilder builder = Client.builder()
                .endpoints(etcdConfig.getEndpoints());

        if (etcdConfig.getUsername() != null && !etcdConfig.getUsername().isEmpty()
                && etcdConfig.getPassword() != null && !etcdConfig.getPassword().isEmpty()) {
            builder.user(io.etcd.jetcd.ByteSequence.from(etcdConfig.getUsername().getBytes()))
                    .password(io.etcd.jetcd.ByteSequence.from(etcdConfig.getPassword().getBytes()));
        }

        log.info("Creating Etcd Client with endpoints: {}", String.join(",", etcdConfig.getEndpoints()));
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(ServiceRegistry.class)
    public ServiceRegistry etcdServiceRegistry(Client etcdClient, RegistryProperties properties) {
        long ttl = properties.getEtcd().getTtl();
        log.info("Creating Etcd ServiceRegistry with TTL: {}s", ttl);
        return new EtcdServiceRegistry(etcdClient, ttl);
    }

    @Bean
    @ConditionalOnMissingBean(ServiceDiscovery.class)
    public ServiceDiscovery etcdServiceDiscovery(Client etcdClient) {
        log.info("Creating Etcd ServiceDiscovery");
        return new EtcdServiceDiscovery(etcdClient);
    }
}

