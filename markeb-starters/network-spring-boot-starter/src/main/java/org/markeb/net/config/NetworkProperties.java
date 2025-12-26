package org.markeb.net.config;

import org.markeb.net.protocol.ProtocolType;
import org.markeb.net.serialization.CodecType;
import org.markeb.net.transport.TransportType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 网络配置属性
 */
@Data
@ConfigurationProperties(prefix = "markeb.network")
public class NetworkProperties {

    /**
     * 是否启用网络模块
     */
    private boolean enabled = true;

    /**
     * 服务器端口
     */
    private int port = 9200;

    /**
     * 传输协议类型
     */
    private TransportType transport = TransportType.TCP;

    /**
     * 协议类型（网关/游戏服）
     */
    private ProtocolType protocol = ProtocolType.GATEWAY;

    /**
     * 编解码类型
     */
    private CodecType codec = CodecType.PROTOBUF;

    /**
     * 最大帧长度
     */
    private int maxFrameLength = 1024 * 1024;

    /**
     * Netty 配置
     */
    private NettyConfig netty = new NettyConfig();

    /**
     * 网关特定配置
     */
    private GatewayConfig gateway = new GatewayConfig();

    /**
     * 游戏服特定配置
     */
    private GameServerConfig gameServer = new GameServerConfig();

    @Data
    public static class NettyConfig {
        /**
         * Boss 线程数
         */
        private int bossThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);

        /**
         * Worker 线程数
         */
        private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;

        /**
         * 读空闲时间（秒）
         */
        private long readerIdleTime = 60;

        /**
         * 写空闲时间（秒）
         */
        private long writerIdleTime = 0;

        /**
         * 读写空闲时间（秒）
         */
        private long allIdleTime = 0;
    }

    @Data
    public static class GatewayConfig {
        /**
         * 是否启用网关模式
         */
        private boolean enabled = false;

        /**
         * 网关ID
         */
        private int gateId = 1;

        /**
         * 魔数
         */
        private int magicNum = 0xABCD;
    }

    @Data
    public static class GameServerConfig {
        /**
         * 是否启用游戏服模式
         */
        private boolean enabled = false;
    }
}

