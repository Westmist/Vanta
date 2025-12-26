package org.markeb.game.event;

import org.markeb.eventbus.Event;

public class PlayerLogin implements Event {

    private long playerId;

    @Override
    public String topic() {
        return "player:login";
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

}
