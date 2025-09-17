package com.game.vanta.bootstrap.handler;

import ace.game.vanta.proto.Test;
import com.game.vanta.net.register.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TestHandler {

    private static final Logger log = LoggerFactory.getLogger(TestHandler.class);

    @MessageHandler
    public Test.ResTestMessage test(Role role, Test.ReqTestMessage reqTestMessage) {
        log.info("TestHandler test method called with message: {}", reqTestMessage);
        return Test.ResTestMessage.newBuilder()
                .setResult(true)
                .build();
    }

    @MessageHandler
    public void testVoid(Role role, Test.ReqTestVoidMessage reqTestVoidMessage) {
        log.info("TestHandler test void method called with message: {}", reqTestVoidMessage);
    }

}
