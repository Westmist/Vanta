package com.game.vanta.gateway.discovery;

import com.alibaba.nacos.client.naming.listener.AbstractNamingChangeListener;
import com.alibaba.nacos.client.naming.listener.NamingChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NacosServiceChangeListener extends AbstractNamingChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(NacosServiceChangeListener.class);

    @Override
    public void onChange(NamingChangeEvent event) {

        System.out.println("服务变化: " + event.getServiceName());
        System.out.println("实例列表: " + event.getInstances());

        // 这里可以处理具体的业务逻辑
        event.getInstances().forEach(instance -> {
            System.out.println("实例: " + instance.getIp() + ":" + instance.getPort() +
                " 健康:" + instance.isHealthy());
        });
    }

}
