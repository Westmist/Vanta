package com.game.vanta.net.handler;

import com.game.vanta.net.netty.NettyProperties;
import com.game.vanta.net.msg.IMessagePool;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import java.util.List;

public interface ChannelInitializerProvider {

    ChannelInitializer<SocketChannel> buildInitializer(NettyProperties properties);

    IMessagePool<?> messagePool();

    List<IBusinessChannelHandler> channelHandlerList();

}
