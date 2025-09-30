package com.game.vanta.net.handler;

import com.game.vanta.net.netty.NettyProperties;
import com.game.vanta.net.msg.IMessagePool;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DefaultChannelInitializer implements ChannelInitializerProvider {

    private final IMessagePool<?> messagePool;

    private final List<IBusinessChannelHandler> businessHandlers;

    public DefaultChannelInitializer(IMessagePool<?> messagePool,
                                     ObjectProvider<List<IBusinessChannelHandler>> businessHandlers) {
        Objects.requireNonNull(messagePool, "Null messagePool not permitted");
        this.messagePool = messagePool;
        this.businessHandlers = businessHandlers.getIfAvailable(ArrayList::new);
    }

    @Override
    public ChannelInitializer<SocketChannel> buildInitializer(NettyProperties properties) {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast("idleStateHandler",
                        new IdleStateHandler(properties.getReaderIdleTime(), properties.getWriterIdleTime(),
                                properties.getAllIdleTime(), TimeUnit.SECONDS));

                ch.pipeline().addLast(messagePool().decoder());
                ch.pipeline().addLast(messagePool().encoder());

                // 调用业务自定义 handlers
                for (IBusinessChannelHandler handler : channelHandlerList()) {
                    handler.addHandlers(ch.pipeline());
                }
            }
        };
    }

    @Override
    public IMessagePool<?> messagePool() {
        return messagePool;
    }

    @Override
    public List<IBusinessChannelHandler> channelHandlerList() {
        return businessHandlers;
    }


}
