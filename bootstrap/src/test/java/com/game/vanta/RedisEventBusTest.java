package com.game.vanta;


import org.vanta.game.event.PlayerLogin;
import org.vanta.game.event.PlayerLogout;
import com.game.vanta.redis.eventbus.pubsub.RedisEventPublisher;
import com.game.vanta.redis.eventbus.pubsub.RedisEventSubscriber;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.vanta.game.BootstrapApplication;

@TestPropertySource(properties = {
    "network.enabled=false",
    "spring.cloud.service-registry.auto-registration.enabled=false"
})
@SpringBootTest(
    classes = {
        BootstrapApplication.class
    }
)
public class RedisEventBusTest {

    @Autowired
    private RedisEventPublisher publisher;

    @Autowired
    private RedisEventSubscriber subscriber;

    @Test
    public void testEvent() {
        PlayerLogin playerLogin = new PlayerLogin();
        publisher.publish(playerLogin);

        PlayerLogout playerLogout = new PlayerLogout();
        publisher.publish(playerLogout);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
