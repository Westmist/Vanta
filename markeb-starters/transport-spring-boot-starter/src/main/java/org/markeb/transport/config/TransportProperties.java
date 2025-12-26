package org.markeb.transport.config;

import org.markeb.transport.TransportType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RPC 传输配置属性
 */
@ConfigurationProperties(prefix = "markeb.transport")
public class TransportProperties {

    /**
     * 是否启用 RPC 传输
     */
    private boolean enabled = true;

    /**
     * 传输类型
     */
    private TransportType type = TransportType.GRPC;

    /**
     * 服务端配置
     */
    private ServerConfig server = new ServerConfig();

    /**
     * 客户端配置
     */
    private ClientConfig client = new ClientConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public TransportType getType() {
        return type;
    }

    public void setType(TransportType type) {
        this.type = type;
    }

    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public ClientConfig getClient() {
        return client;
    }

    public void setClient(ClientConfig client) {
        this.client = client;
    }

    public static class ServerConfig {
        /**
         * 是否启用服务端
         */
        private boolean enabled = true;

        /**
         * 监听端口
         */
        private int port = 9090;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class ClientConfig {
        /**
         * 是否启用客户端
         */
        private boolean enabled = true;

        /**
         * 调用超时时间（毫秒）
         */
        private int timeout = 5000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }
    }
}

