package org.markeb.service.registry.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import org.markeb.service.registry.RegistryType;
import org.markeb.service.registry.ServiceDiscovery;
import org.markeb.service.registry.ServiceRegistry;
import org.markeb.service.registry.nacos.NacosServiceDiscovery;
import org.markeb.service.registry.nacos.NacosServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Nacos 自动配置
 */
@Slf4j
@Configuration
@ConditionalOnClass(NamingService.class)
@ConditionalOnProperty(prefix = "markeb.registry", name = "type", havingValue = "nacos", matchIfMissing = true)
@EnableConfigurationProperties(RegistryProperties.class)
public class NacosAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public NamingService nacosNamingService(RegistryProperties properties) throws NacosException {
        RegistryProperties.NacosConfig nacosConfig = properties.getNacos();

        Properties nacosProperties = new Properties();
        nacosProperties.setProperty("serverAddr", nacosConfig.getServerAddr());
        nacosProperties.setProperty("namespace", nacosConfig.getNamespace());

        if (nacosConfig.getUsername() != null && !nacosConfig.getUsername().isEmpty()) {
            nacosProperties.setProperty("username", nacosConfig.getUsername());
        }
        if (nacosConfig.getPassword() != null && !nacosConfig.getPassword().isEmpty()) {
            nacosProperties.setProperty("password", nacosConfig.getPassword());
        }

        log.info("Creating Nacos NamingService with serverAddr: {}", nacosConfig.getServerAddr());
        return NacosFactory.createNamingService(nacosProperties);
    }

    @Bean
    @ConditionalOnMissingBean(ServiceRegistry.class)
    public ServiceRegistry nacosServiceRegistry(NamingService namingService, RegistryProperties properties) {
        String group = properties.getNacos().getGroup();
        log.info("Creating Nacos ServiceRegistry with group: {}", group);
        return new NacosServiceRegistry(namingService, group);
    }

    @Bean
    @ConditionalOnMissingBean(ServiceDiscovery.class)
    public ServiceDiscovery nacosServiceDiscovery(NamingService namingService, RegistryProperties properties) {
        String group = properties.getNacos().getGroup();
        log.info("Creating Nacos ServiceDiscovery with group: {}", group);
        return new NacosServiceDiscovery(namingService, group);
    }
}

