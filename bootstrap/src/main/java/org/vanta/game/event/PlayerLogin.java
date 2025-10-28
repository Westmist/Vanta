package org.vanta.game.event;

import com.game.vanta.redis.eventbus.RedisEvent;

public class PlayerLogin implements RedisEvent {

    private String playerId;

    @Override
    public String channel() {
        return "player:login";
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

}
