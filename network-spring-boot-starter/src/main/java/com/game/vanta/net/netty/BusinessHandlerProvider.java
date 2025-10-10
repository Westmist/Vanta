package com.game.vanta.net.netty;

import io.netty.channel.ChannelHandler;

import java.util.List;
import java.util.function.Supplier;

public interface BusinessHandlerProvider {
    List<Supplier<ChannelHandler>> businessHandlers();
}
