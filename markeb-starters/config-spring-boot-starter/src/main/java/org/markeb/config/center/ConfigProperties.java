package org.markeb.config.center;

import org.markeb.config.ConfigType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置中心配置属性
 */
@ConfigurationProperties(prefix = "markeb.config")
public class ConfigProperties {

    /**
     * 是否启用配置中心
     */
    private boolean enabled = true;

    /**
     * 配置中心类型
     */
    private ConfigType type = ConfigType.NACOS;

    /**
     * Nacos 配置
     */
    private NacosConfig nacos = new NacosConfig();

    /**
     * Etcd 配置
     */
    private EtcdConfig etcd = new EtcdConfig();

    /**
     * Consul 配置
     */
    private ConsulConfig consul = new ConsulConfig();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ConfigType getType() {
        return type;
    }

    public void setType(ConfigType type) {
        this.type = type;
    }

    public NacosConfig getNacos() {
        return nacos;
    }

    public void setNacos(NacosConfig nacos) {
        this.nacos = nacos;
    }

    public EtcdConfig getEtcd() {
        return etcd;
    }

    public void setEtcd(EtcdConfig etcd) {
        this.etcd = etcd;
    }

    public ConsulConfig getConsul() {
        return consul;
    }

    public void setConsul(ConsulConfig consul) {
        this.consul = consul;
    }

    public static class NacosConfig {
        /**
         * 服务地址
         */
        private String serverAddr = "localhost:8848";

        /**
         * 命名空间
         */
        private String namespace = "";

        /**
         * 分组
         */
        private String group = "DEFAULT_GROUP";

        /**
         * 超时时间（毫秒）
         */
        private long timeout = 5000;

        public String getServerAddr() {
            return serverAddr;
        }

        public void setServerAddr(String serverAddr) {
            this.serverAddr = serverAddr;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }
    }

    public static class EtcdConfig {
        /**
         * 端点列表
         */
        private String endpoints = "http://localhost:2379";

        /**
         * 用户名
         */
        private String username;

        /**
         * 密码
         */
        private String password;

        /**
         * 配置前缀
         */
        private String prefix = "/markeb/config/";

        public String getEndpoints() {
            return endpoints;
        }

        public void setEndpoints(String endpoints) {
            this.endpoints = endpoints;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }

    public static class ConsulConfig {
        /**
         * 主机
         */
        private String host = "localhost";

        /**
         * 端口
         */
        private int port = 8500;

        /**
         * 配置前缀
         */
        private String prefix = "markeb/config/";

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }
}

