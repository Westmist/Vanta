package org.markeb.service.registry;

import java.util.List;

/**
 * 服务变更监听器
 */
@FunctionalInterface
public interface ServiceChangeListener {

    /**
     * 服务实例变更回调
     *
     * @param serviceName 服务名称
     * @param instances   最新的服务实例列表
     */
    void onServiceChange(String serviceName, List<ServiceInstance> instances);
}

