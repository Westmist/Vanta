package com.game.vanta.netty;

import com.game.vanta.net.msg.IMessagePool;
import com.game.vanta.net.netty.BusinessHandlerProvider;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandler;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class ServerChannelInitializerProvider implements BusinessHandlerProvider {

  private final IMessagePool<Message> messagePool;

  public ServerChannelInitializerProvider(IMessagePool<Message> messagePool) {
    this.messagePool = messagePool;
  }

  @Override
  public List<Supplier<ChannelHandler>> businessHandlers() {
    return List.of(() -> new ServerHandler(messagePool));
  }
}
