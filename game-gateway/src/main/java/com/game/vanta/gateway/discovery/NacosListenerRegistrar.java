package com.game.vanta.gateway.discovery;

import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class NacosListenerRegistrar implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(NacosServiceChangeListener.class);

    private final NacosServiceManager nacosServiceManager;

    private final NacosServiceChangeListener nacosServiceChangeListener;

    public NacosListenerRegistrar(
        NacosServiceManager nacosServiceManager,
        NacosServiceChangeListener nacosServiceChangeListener) {
        this.nacosServiceManager = nacosServiceManager;
        this.nacosServiceChangeListener = nacosServiceChangeListener;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        NamingService namingService = nacosServiceManager.getNamingService();
        try {
            namingService.subscribe("vanta-game", "TEST_GROUP", nacosServiceChangeListener);
        } catch (NacosException e) {
            log.error("Failed to subscribe to Nacos service changes", e);
            throw new RuntimeException(e);
        }
    }

}
