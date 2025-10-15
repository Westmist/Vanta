package com.game.vanta.net.netty;

import com.game.vanta.net.INetworkServer;
import com.game.vanta.net.NetworkProperties;
import com.game.vanta.net.handler.ChannelInitializerProvider;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer implements INetworkServer {

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    private final NetworkProperties networkProperties;

    private final NettyProperties nettyProperties;

    private final ChannelInitializerProvider initializerProvider;

    private Channel serverChannel;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    public NettyServer(
                       NetworkProperties networkProperties, NettyProperties nettyProperties, ChannelInitializerProvider initializerProvider) {
        this.networkProperties = networkProperties;
        this.nettyProperties = nettyProperties;
        this.initializerProvider = initializerProvider;
    }

    private WriteBufferWaterMark writeBufferWaterMark() {
        return new WriteBufferWaterMark(256 * 1024, 512 * 1024);
    }

    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(nettyProperties.getBossThreads());
        workerGroup = new NioEventLoopGroup(nettyProperties.getWorkerThreads());
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_REUSEADDR, true).childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE, true).childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT).childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, writeBufferWaterMark());

            ChannelInitializer<SocketChannel> initializer = initializerProvider.buildInitializer(nettyProperties);
            bootstrap.childHandler(initializer);

            ChannelFuture channelFuture = bootstrap.bind(networkProperties.getPort()).sync();
            this.serverChannel = channelFuture.channel();
            log.info("Netty started at port {}", networkProperties.getPort());
        } catch (Exception e) {
            log.error("Netty server start failed on port {}", networkProperties.getPort(), e);
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
