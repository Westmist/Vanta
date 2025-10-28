package org.vanta.game.event.handler;

import org.vanta.game.event.PlayerLogin;
import org.vanta.game.event.PlayerLogout;
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
