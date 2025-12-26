package org.markeb.game;

import org.markeb.net.EnableMessageHandlerScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMessageHandlerScan(
        handlerPackages = "org.markeb.game.handler",
        messagePackages = "org.markeb.proto")
public class NodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(NodeApplication.class, args);
    }

}
