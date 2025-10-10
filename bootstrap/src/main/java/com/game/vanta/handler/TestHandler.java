package com.game.vanta.handler;

import ace.game.vanta.proto.Test;
import com.game.vanta.actor.Player;
import com.game.vanta.dao.Role;
import com.game.vanta.net.register.MessageHandler;
import com.game.vanta.persistent.DataCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class TestHandler {

    private static final Logger log = LoggerFactory.getLogger(TestHandler.class);

    @MessageHandler
    public Test.ResTestMessage test(Player player, Test.ReqTestMessage reqTestMessage) {
        log.info("TestHandler test method called with message: {}", reqTestMessage);

        Role role = new Role();
        role.setId(String.valueOf(new Random().nextLong()));
        role.setName("Hero");
        DataCenter.service.upsertAsync(role);

        return Test.ResTestMessage.newBuilder()
                .setResult(true)
                .build();
    }

    @MessageHandler
    public void testVoid(Player player, Test.ReqTestVoidMessage reqTestVoidMessage) {
        log.info("TestHandler test void method called with message: {}", reqTestVoidMessage);
    }

}
