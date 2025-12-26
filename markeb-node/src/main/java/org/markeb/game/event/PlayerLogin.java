package org.markeb.game.event;

import org.markeb.eventbus.Event;

public class PlayerLogin implements Event {

    private String playerId;

    @Override
    public String topic() {
        return "player:login";
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

}
