package com.game.vanta.manager;

import com.game.vanta.net.register.GameActorContext;
import io.netty.channel.Channel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private final Map<Channel, GameActorContext> playerMap = new ConcurrentHashMap<>();

    private static final PlayerManager INSTANCE = new PlayerManager();

    private PlayerManager() {
    }

    public static PlayerManager getInstance() {
        return INSTANCE;
    }

    public <T extends GameActorContext> void addPlayer(Channel channel, T t) {
        playerMap.put(channel, t);
    }

    public void removePlayer(Channel channel) {
        playerMap.remove(channel);
    }

    @SuppressWarnings("unchecked")
    public <T extends GameActorContext> T getPlayer(Channel channel) {
        return (T) playerMap.get(channel);
    }
}
