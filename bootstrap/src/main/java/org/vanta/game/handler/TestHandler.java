package org.vanta.game.handler;

import org.vanta.game.actor.Player;
import com.game.vanta.net.register.MessageHandler;
import com.game.vanta.persistent.DataCenter;
import com.game.vanta.persistent.entity.Role;
import com.game.vanta.proto.Test;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class TestHandler {

    private static final Logger log = LoggerFactory.getLogger(TestHandler.class);

    @MessageHandler
    public Test.ResTestMessage test(Player player, Test.ReqTestMessage reqTestMessage) {
        log.info("TestHandler test method called with message: {}", reqTestMessage);

        long timestamp = System.currentTimeMillis();
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatted = dateTime.format(formatter);

        Role role = new Role();
        ObjectId objectId = new ObjectId();
        role.setId(objectId.toHexString());
        role.setName(formatted);
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
