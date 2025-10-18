package com.game.vanta.event;

import com.game.vanta.redis.eventbus.RedisEvent;

public class PlayerLogout implements RedisEvent {

    private  String playerId;

    @Override
    public String channel() {
        return "player:logout";
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
