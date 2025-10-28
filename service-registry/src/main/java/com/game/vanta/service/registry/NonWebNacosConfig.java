package com.game.vanta.service.registry;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties
@ConditionalOnNacosDiscoveryEnabled
public class NonWebNacosConfig {

    /**
     * @see NacosServiceRegistryAutoConfiguration#nacosAutoServiceRegistration(NacosServiceRegistry, AutoServiceRegistrationProperties, NacosRegistration)
     */
    @Bean
    @ConditionalOnBean(AutoServiceRegistrationProperties.class)
    public NacosNonWebAutoRegistrar nacosNonWebAutoRegistrar(
        NacosAutoServiceRegistration registration) {
        return new NacosNonWebAutoRegistrar(registration);
    }


}
