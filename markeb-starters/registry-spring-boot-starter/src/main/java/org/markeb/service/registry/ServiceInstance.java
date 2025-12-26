package org.markeb.service.registry;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 服务实例信息
 */
@Data
@Builder
public class ServiceInstance {

    /**
     * 服务实例唯一标识
     */
    private String instanceId;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务主机地址
     */
    private String host;

    /**
     * 服务端口
     */
    private int port;

    /**
     * 服务权重
     */
    @Builder.Default
    private double weight = 1.0;

    /**
     * 是否健康
     */
    @Builder.Default
    private boolean healthy = true;

    /**
     * 是否启用
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * 服务元数据
     */
    private Map<String, String> metadata;

    /**
     * 获取服务地址 host:port
     */
    public String getAddress() {
        return host + ":" + port;
    }
}

