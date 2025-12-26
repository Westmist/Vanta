package org.markeb.service.registry;

import java.util.List;

/**
 * 服务注册接口
 */
public interface ServiceRegistry {

    /**
     * 注册服务实例
     *
     * @param instance 服务实例
     */
    void register(ServiceInstance instance);

    /**
     * 注销服务实例
     *
     * @param instance 服务实例
     */
    void deregister(ServiceInstance instance);

    /**
     * 获取所有服务实例
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
     * 关闭注册中心连接
     */
    void close();
}

