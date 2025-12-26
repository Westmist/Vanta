package org.markeb.game.actor;

import org.markeb.net.register.GameActorContext;
import io.netty.channel.Channel;


public class Player implements GameActorContext {

    private final Channel channel;

    public Player(Channel channel) {
        this.channel = channel;
    }

    // 每一个 Actor 都有需要持有 Actor 执行器，这个执行器负责处理该 Actor 的所有消息
    // 由唯一不可变 key 去选择 Actor 执行器
    //



}
