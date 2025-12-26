package org.markeb.config.center;

import com.alibaba.nacos.api.exception.NacosException;
import org.markeb.config.ConfigService;
import org.markeb.config.local.LocalConfigService;
import org.markeb.config.nacos.NacosConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置中心自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(ConfigProperties.class)
@ConditionalOnProperty(prefix = "markeb.config", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ConfigAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ConfigAutoConfiguration.class);

    @Configuration
    @ConditionalOnClass(name = "com.alibaba.nacos.api.config.ConfigService")
    @ConditionalOnProperty(prefix = "markeb.config", name = "type", havingValue = "NACOS", matchIfMissing = true)
    static class NacosConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ConfigService nacosConfigService(ConfigProperties properties) throws NacosException {
            ConfigProperties.NacosConfig nacos = properties.getNacos();
            log.info("Initializing Nacos config service with server: {}", nacos.getServerAddr());
            return new NacosConfigService(
                    nacos.getServerAddr(),
                    nacos.getNamespace(),
                    nacos.getGroup(),
                    nacos.getTimeout()
            );
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "markeb.config", name = "type", havingValue = "LOCAL")
    static class LocalConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ConfigService localConfigService() {
            log.info("Initializing Local config service (for testing only)");
            return new LocalConfigService();
        }
    }
}

