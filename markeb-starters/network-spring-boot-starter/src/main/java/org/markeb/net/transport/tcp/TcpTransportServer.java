package org.markeb.net.transport.tcp;

import org.markeb.net.transport.TransportServer;
import org.markeb.net.transport.TransportType;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP 传输服务器实现
 */
public class TcpTransportServer implements TransportServer {

    private static final Logger log = LoggerFactory.getLogger(TcpTransportServer.class);

    private final int port;
    private final int bossThreads;
    private final int workerThreads;
    private final ChannelInitializer<SocketChannel> channelInitializer;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public TcpTransportServer(int port, int bossThreads, int workerThreads,
                               ChannelInitializer<SocketChannel> channelInitializer) {
        this.port = port;
        this.bossThreads = bossThreads;
        this.workerThreads = workerThreads;
        this.channelInitializer = channelInitializer;
    }

    @Override
    public void start() throws Exception {
        if (!running.compareAndSet(false, true)) {
            log.warn("TCP server already running on port {}", port);
            return;
        }

        bossGroup = new NioEventLoopGroup(bossThreads);
        workerGroup = new NioEventLoopGroup(workerThreads);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK,
                            new WriteBufferWaterMark(256 * 1024, 512 * 1024))
                    .childHandler(channelInitializer);

            ChannelFuture future = bootstrap.bind(port).sync();
            serverChannel = future.channel();
            log.info("TCP server started on port {}", port);
        } catch (Exception e) {
            running.set(false);
            stop();
            throw e;
        }
    }

    @Override
    public void stop() {
        log.info("Stopping TCP server on port {}...", port);
        running.set(false);

        try {
            if (serverChannel != null) {
                serverChannel.close().sync();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            log.info("TCP server stopped");
        }
    }

    @Override
    public TransportType getTransportType() {
        return TransportType.TCP;
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int getPort() {
        return port;
    }
}

