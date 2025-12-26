package org.markeb.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 网关配置
 */
@Configuration
@ConfigurationProperties(prefix = "markeb.gateway")
public class GatewayConfig {

    /**
     * 网关监听端口
     */
    private int port = 8080;

    /**
     * 工作线程数
     */
    private int workerThreads = 0;

    /**
     * 读空闲超时（秒）
     */
    private int readIdleTimeout = 120;

    /**
     * 最大连接数
     */
    private int maxConnections = 10000;

    /**
     * 静态节点配置（nodeId -> host:port）
     */
    private Map<String, String> nodes = new HashMap<>();

    /**
     * 默认路由策略
     */
    private String routeStrategy = "ROUND_ROBIN";

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWorkerThreads() {
        return workerThreads;
    }

    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }

    public int getReadIdleTimeout() {
        return readIdleTimeout;
    }

    public void setReadIdleTimeout(int readIdleTimeout) {
        this.readIdleTimeout = readIdleTimeout;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public Map<String, String> getNodes() {
        return nodes;
    }

    public void setNodes(Map<String, String> nodes) {
        this.nodes = nodes;
    }

    public String getRouteStrategy() {
        return routeStrategy;
    }

    public void setRouteStrategy(String routeStrategy) {
        this.routeStrategy = routeStrategy;
    }
}

