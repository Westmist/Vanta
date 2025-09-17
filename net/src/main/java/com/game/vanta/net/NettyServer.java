package com.game.vanta.net;

import com.game.vanta.net.handler.ChannelInitializerProvider;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    private final NettyProperties properties;

    private final ChannelInitializerProvider initializerProvider;

    private Channel serverChannel;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    public NettyServer(NettyProperties properties,
                       ChannelInitializerProvider initializerProvider) {
        this.properties = properties;
        this.initializerProvider = initializerProvider;
    }

    private WriteBufferWaterMark writeBufferWaterMark() {
        return new WriteBufferWaterMark(256 * 1024, 512 * 1024);
    }

    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(properties.getBossThreads());
        workerGroup = new NioEventLoopGroup(properties.getWorkerThreads());
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, writeBufferWaterMark());

            ChannelInitializer<SocketChannel> initializer = initializerProvider.buildInitializer(properties);
            bootstrap.childHandler(initializer);

            ChannelFuture channelFuture = bootstrap.bind(properties.getPort()).sync();
            this.serverChannel = channelFuture.channel();
            log.info("Netty started at port {}", properties.getPort());
        } catch (Exception e) {
            log.error("Netty server start failed on port {}", properties.getPort(), e);
            stop();
            throw e;
        }
    }

    public void stop() {
        log.info("Shutting down Netty server...");
        try {
            if (serverChannel != null) {
                serverChannel.close().sync();
                log.info("Server channel closed");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().syncUninterruptibly();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().syncUninterruptibly();
            }
            log.info("Netty server shutdown complete");
        }
    }

}

