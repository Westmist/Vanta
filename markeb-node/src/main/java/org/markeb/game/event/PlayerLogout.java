package org.markeb.game.event;

import org.markeb.eventbus.Event;

public class PlayerLogout implements Event {

    private long playerId;

    @Override
    public String topic() {
        return "player:logout";
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }
}
