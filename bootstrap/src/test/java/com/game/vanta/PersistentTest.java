package com.game.vanta;


import com.game.vanta.net.NetworkAutoConfiguration;
import com.game.vanta.persistent.PersistentUtil;
import com.game.vanta.persistent.dao.IPersistent;
import com.game.vanta.persistent.entity.Backpack;
import com.game.vanta.persistent.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.vanta.game.BootstrapApplication;

@TestPropertySource(properties = {
    "network.enabled=false",
    "spring.cloud.service-registry.auto-registration.enabled=false"
})
@SpringBootTest(
    classes = {
        NetworkAutoConfiguration.class,
        BootstrapApplication.class
    }
)
public class PersistentTest {

    @Autowired
    private RedisTemplate<String, IPersistent> redisTemplate;

    @Test
    public void testPersistentCodec() {

        Role role = new Role();
        role.setId("mx");
        role.setName("Test");
        String roleKey = PersistentUtil.build(role.getClass().getSimpleName(), role.getId());
        redisTemplate.opsForValue().set(roleKey, role);
        Role mxR = (Role) redisTemplate.opsForValue().get(roleKey);

        Backpack backpack = new Backpack();
        backpack.setId("mx");
        backpack.getItems().put("item1", "Sword");
        String backpackKey = PersistentUtil.build(backpack.getClass().getSimpleName(), backpack.getId());
        redisTemplate.opsForValue().set(backpackKey, backpack);
        Backpack mxB = (Backpack) redisTemplate.opsForValue().get(backpackKey);


        System.out.println(mxB);

    }


}
