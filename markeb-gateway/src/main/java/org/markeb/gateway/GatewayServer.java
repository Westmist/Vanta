package org.markeb.gateway;

import org.markeb.gateway.backend.BackendChannelManager;
import org.markeb.gateway.config.GatewayConfig;
import org.markeb.gateway.handler.FrontendChannelInitializer;
import org.markeb.gateway.route.NodeRouter;
import org.markeb.gateway.session.SessionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * 网关服务器
 */
@Component
public class GatewayServer {

    private static final Logger log = LoggerFactory.getLogger(GatewayServer.class);

    @Autowired
    private GatewayConfig config;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private BackendChannelManager backendChannelManager;

    @Autowired
    private NodeRouter nodeRouter;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    @PostConstruct
    public void start() {
        // 初始化静态节点配置
        config.getNodes().forEach(nodeRouter::addStaticNode);

        // 启动服务器
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = config.getWorkerThreads() > 0 
                ? new NioEventLoopGroup(config.getWorkerThreads())
                : new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new FrontendChannelInitializer(
                        sessionManager,
                        backendChannelManager,
                        nodeRouter,
                        config.getReadIdleTimeout()
                ));

        try {
            ChannelFuture future = bootstrap.bind(config.getPort()).sync();
            serverChannel = future.channel();
            log.info("Gateway server started on port {}", config.getPort());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to start gateway server", e);
        }
    }

    @PreDestroy
    public void stop() {
        log.info("Stopping gateway server...");

        if (serverChannel != null) {
            serverChannel.close();
        }

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        log.info("Gateway server stopped");
    }

    /**
     * 获取当前连接数
     */
    public int getConnectionCount() {
        return sessionManager.getSessionCount();
    }

    /**
     * 获取已认证连接数
     */
    public long getAuthenticatedCount() {
        return sessionManager.getAuthenticatedCount();
    }
}

