package org.markeb.service.registry.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 服务注册自动配置入口
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "markeb.registry", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RegistryProperties.class)
@Import({
        NacosAutoConfiguration.class,
        EtcdAutoConfiguration.class,
        ConsulAutoConfiguration.class,
        ServiceAutoRegistrar.class
})
public class RegistryAutoConfiguration {
}

