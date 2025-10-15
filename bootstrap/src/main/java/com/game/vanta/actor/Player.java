package com.game.vanta.actor;

import com.game.vanta.net.register.GameActorContext;
import io.netty.channel.Channel;

public class Player implements GameActorContext {

    private final Channel channel;

    public Player(Channel channel) {
        this.channel = channel;
    }
}
