package com.game.vanta.event.handler;

import com.game.vanta.event.PlayerLogin;
import com.game.vanta.event.PlayerLogout;
import com.game.vanta.redis.eventbus.RedisEventAction;
import org.springframework.stereotype.Component;

@Component
public class EventHandler {

    @RedisEventAction
    public void playerLogout(PlayerLogout event) {
        System.out.println("Player logged out.");
    }

    @RedisEventAction
    public void playerLogin(PlayerLogin event) {
        System.out.println("Player logged in.");
    }

}
