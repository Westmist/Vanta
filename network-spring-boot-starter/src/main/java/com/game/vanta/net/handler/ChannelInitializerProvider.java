package com.game.vanta.net.handler;

import com.game.vanta.net.msg.IMessagePool;
import com.game.vanta.net.netty.BusinessHandlerProvider;
import com.game.vanta.net.netty.NettyProperties;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public interface ChannelInitializerProvider {

  ChannelInitializer<SocketChannel> buildInitializer(NettyProperties properties);

  IMessagePool<?> messagePool();

  BusinessHandlerProvider handlerProvider();
}
