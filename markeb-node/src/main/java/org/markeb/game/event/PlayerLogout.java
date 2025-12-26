package org.markeb.game.event;

import org.markeb.eventbus.Event;

public class PlayerLogout implements Event {

    private String playerId;

    @Override
    public String topic() {
        return "player:logout";
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
