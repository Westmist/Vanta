package com.game.vanta.net.handler;

import io.netty.channel.ChannelPipeline;

public interface IBusinessChannelHandler {
    void addHandlers(ChannelPipeline pipeline);
}
