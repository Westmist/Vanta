package org.markeb.game.event.handler;

import org.markeb.game.event.PlayerLogin;
import org.markeb.game.event.PlayerLogout;
import org.markeb.eventbus.annotation.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EventHandler {

    @EventListener
    public void playerLogout(PlayerLogout event) {
        System.out.println("Player logged out.");
    }

    @EventListener
    public void playerLogin(PlayerLogin event) {
        System.out.println("Player logged in.");
    }

}
