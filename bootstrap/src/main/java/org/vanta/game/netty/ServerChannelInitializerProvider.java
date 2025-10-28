package org.vanta.game.netty;

import com.game.vanta.net.msg.IMessagePool;
import com.game.vanta.net.netty.BusinessHandlerProvider;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

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
