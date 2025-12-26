package org.markeb.service.registry;

import java.util.List;

/**
 * 服务发现接口
 */
public interface ServiceDiscovery {

    /**
     * 获取服务实例列表
     *
     * @param serviceName 服务名称
     * @return 服务实例列表
     */
    List<ServiceInstance> getInstances(String serviceName);

    /**
     * 获取所有服务名称
     *
     * @return 服务名称列表
     */
    List<String> getServices();

    /**
     * 订阅服务变更
     *
     * @param serviceName 服务名称
     * @param listener    服务变更监听器
     */
    void subscribe(String serviceName, ServiceChangeListener listener);

    /**
     * 取消订阅服务变更
     *
     * @param serviceName 服务名称
     * @param listener    服务变更监听器
     */
    void unsubscribe(String serviceName, ServiceChangeListener listener);

    /**
     * 关闭服务发现
     */
    void close();
}

