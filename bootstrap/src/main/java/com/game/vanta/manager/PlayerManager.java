package com.game.vanta.manager;

import com.game.vanta.net.register.GameActorContext;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private final Map<String, GameActorContext> playerMap = new ConcurrentHashMap<>();

    private static final PlayerManager INSTANCE = new PlayerManager();

    private PlayerManager() {

    }

    public static PlayerManager getInstance() {
        return INSTANCE;
    }

    public void removePlayer(String id) {
        playerMap.remove(id);
    }

}
