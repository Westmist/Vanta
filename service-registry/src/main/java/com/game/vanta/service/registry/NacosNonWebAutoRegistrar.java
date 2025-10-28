package com.game.vanta.service.registry;

import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import com.game.vanta.common.event.NetworkStartedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Component;


public class NacosNonWebAutoRegistrar implements ApplicationListener<ApplicationReadyEvent> {

    private final NacosAutoServiceRegistration registration;

    public NacosNonWebAutoRegistrar(NacosAutoServiceRegistration registration) {
        this.registration = registration;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        registration.start();
    }

}
