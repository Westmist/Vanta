package org.markeb.net.transport.kcp;

import org.markeb.net.transport.TransportServer;
import org.markeb.net.transport.TransportType;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * KCP 传输服务器实现
 * 基于 UDP + KCP 协议实现可靠传输
 * 
 * 注意：完整的 KCP 实现需要引入 kcp-netty 依赖
 * 这里提供基础的 UDP 框架，KCP 协议层需要额外集成
 */
public class KcpTransportServer implements TransportServer {

    private static final Logger log = LoggerFactory.getLogger(KcpTransportServer.class);

    private final int port;
    private final int workerThreads;
    private final ChannelHandler channelHandler;

    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public KcpTransportServer(int port, int workerThreads, ChannelHandler channelHandler) {
        this.port = port;
        this.workerThreads = workerThreads;
        this.channelHandler = channelHandler;
    }

    @Override
    public void start() throws Exception {
        if (!running.compareAndSet(false, true)) {
            log.warn("KCP server already running on port {}", port);
            return;
        }

        workerGroup = new NioEventLoopGroup(workerThreads);

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, false)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(2048))
                    .handler(channelHandler);

            ChannelFuture future = bootstrap.bind(port).sync();
            serverChannel = future.channel();
            log.info("KCP server started on port {} (UDP)", port);
        } catch (Exception e) {
            running.set(false);
            stop();
            throw e;
        }
    }

    @Override
    public void stop() {
        log.info("Stopping KCP server on port {}...", port);
        running.set(false);

        try {
            if (serverChannel != null) {
                serverChannel.close().sync();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            log.info("KCP server stopped");
        }
    }

    @Override
    public TransportType getTransportType() {
        return TransportType.KCP;
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

