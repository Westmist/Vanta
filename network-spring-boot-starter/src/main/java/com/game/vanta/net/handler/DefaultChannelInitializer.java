package com.game.vanta.net.handler;

import com.game.vanta.net.msg.IMessagePool;
import com.game.vanta.net.netty.BusinessHandlerProvider;
import com.game.vanta.net.netty.NettyProperties;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class DefaultChannelInitializer implements ChannelInitializerProvider {

  private final IMessagePool<?> messagePool;

  private final BusinessHandlerProvider handlerProvider;

  public DefaultChannelInitializer(
      IMessagePool<?> messagePool, BusinessHandlerProvider handlerProvider) {
    Objects.requireNonNull(messagePool, "Null messagePool not permitted");
    this.messagePool = messagePool;
    this.handlerProvider = handlerProvider;
  }

  @Override
  public ChannelInitializer<SocketChannel> buildInitializer(NettyProperties properties) {
    return new ChannelInitializer<>() {
      @Override
      protected void initChannel(SocketChannel ch) {
        ch.pipeline()
            .addLast(
                "idleStateHandler",
                new IdleStateHandler(
                    properties.getReaderIdleTime(),
                    properties.getWriterIdleTime(),
                    properties.getAllIdleTime(),
                    TimeUnit.SECONDS));

        ch.pipeline().addLast(messagePool().decoder());
        ch.pipeline().addLast(messagePool().encoder());

        // 调用业务自定义 handlers
        for (Supplier<ChannelHandler> supplier : handlerProvider().businessHandlers()) {
          ch.pipeline().addLast(supplier.get());
        }
      }
    };
  }

  @Override
  public IMessagePool<?> messagePool() {
    return messagePool;
  }

  @Override
  public BusinessHandlerProvider handlerProvider() {
    return handlerProvider;
  }
}
