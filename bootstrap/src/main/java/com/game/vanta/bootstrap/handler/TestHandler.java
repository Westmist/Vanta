package com.game.vanta.bootstrap.handler;

import ace.game.vanta.proto.Test;
import com.game.vanta.net.register.MessageHandler;
import org.springframework.stereotype.Component;

@Component
public class TestHandler {

    @MessageHandler
    public Test.ResTestMessage test(Role role, Test.ReqTestMessage reqTestMessage) {
        System.out.println("TestHandler test method called");
        return Test.ResTestMessage.newBuilder()
                .setResult(true)
                .build();
    }

}
