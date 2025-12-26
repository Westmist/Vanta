package org.markeb.net.handler;

import org.markeb.net.netty.BusinessHandlerProvider;
import org.markeb.net.netty.NettyProperties;
import org.markeb.net.msg.IMessagePool;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.util.List;

public interface ChannelInitializerProvider {

    ChannelInitializer<SocketChannel> buildInitializer(NettyProperties properties);

    IMessagePool<?> messagePool();

    BusinessHandlerProvider handlerProvider();

}
