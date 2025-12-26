package org.markeb.net.gateway.handler;

import org.markeb.net.gateway.backend.BackendConnectionManager;
import org.markeb.net.gateway.codec.GatewayDecoder;
import org.markeb.net.gateway.codec.GatewayEncoder;
import org.markeb.net.handler.ChannelInitializerProvider;
import org.markeb.net.msg.IMessagePool;
import org.markeb.net.netty.BusinessHandlerProvider;
import org.markeb.net.netty.NettyProperties;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * 网关模式的 ChannelInitializer：解析协议头后转发到后端。
 */
public class GatewayChannelInitializer implements ChannelInitializerProvider {

    private final BackendConnectionManager connectionManager;

    public GatewayChannelInitializer(BackendConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public ChannelInitializer<SocketChannel> buildInitializer(NettyProperties properties) {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast("idleStateHandler",
                    new IdleStateHandler(properties.getReaderIdleTime(),
                        properties.getWriterIdleTime(),
                        properties.getAllIdleTime(),
                        TimeUnit.SECONDS));
                ch.pipeline().addLast(new GatewayDecoder());
                ch.pipeline().addLast(new GatewayEncoder());
                ch.pipeline().addLast(new GatewayDispatchHandler(connectionManager));
            }
        };
    }

    @Override
    public IMessagePool<?> messagePool() {
        return null;
    }

    @Override
    public BusinessHandlerProvider handlerProvider() {
        return null;
    }


}

