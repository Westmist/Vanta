package org.markeb.game.manager;

import org.markeb.net.register.GameActorContext;

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
