package com.game.vanta.gateway;

import com.game.vanta.net.EnableMessageHandlerScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableMessageHandlerScan(
    handlerPackages = "org.vanta.game.handler",
    messagePackages = "com.game.vanta.proto")
@SpringBootApplication
public class GameGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameGatewayApplication.class, args);
    }

}
