package com.game.vanta.bootstrap;

import com.game.vanta.bootstrap.handler.Role;
import com.game.vanta.persistent.PersistentAutoConfiguration;
import com.game.vanta.persistent.PersistentTemplate;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = {
        PersistentAutoConfiguration.class
})
class PersistentApplicationTests {

    @Autowired
    private PersistentTemplate persistentTemplate;

    @Test
    void contextLoads() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            Role role = new Role();
            role.setId(ObjectId.get().toHexString());
            role.setName("TestRole");
            persistentTemplate.updateAsync(role);
        }, 0, 1, TimeUnit.SECONDS);

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
