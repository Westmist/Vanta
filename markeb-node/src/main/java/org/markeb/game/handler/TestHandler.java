package org.markeb.game.handler;

import org.markeb.game.actor.Player;
import org.markeb.net.register.MessageHandler;
import org.markeb.proto.message.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TestHandler {

    private static final Logger log = LoggerFactory.getLogger(TestHandler.class);

    @MessageHandler
    public Test.ResTestMessage test(Player player, Test.ReqTestMessage reqTestMessage) {
        log.info("TestHandler test method called with message: {}", reqTestMessage);

        // TODO: 使用新的 persistent-spring-boot-starter 实现持久化逻辑
        // DataCenter.saveAsync(entity);

        return Test.ResTestMessage.newBuilder()
                .setResult(true)
                .build();
    }

    @MessageHandler
    public void testVoid(Player player, Test.ReqTestVoidMessage reqTestVoidMessage) {
        log.info("TestHandler test void method called with message: {}", reqTestVoidMessage);
    }

}
