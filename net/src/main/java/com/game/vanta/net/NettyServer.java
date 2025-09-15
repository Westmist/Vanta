package com.game.vanta.net;

import com.game.vanta.net.handler.ChannelInitializerProvider;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServer {

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    private final NettyProperties properties;

    private final ChannelInitializerProvider initializerProvider;

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
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);

        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, writeBufferWaterMark());

        ChannelInitializer<SocketChannel> initializer = initializerProvider.buildInitializer(properties);
        bootstrap.childHandler(initializer);
        bootstrap.bind(properties.getPort()).sync();
        log.info("Netty started at port {}", properties.getPort());
    }

    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}

