package com.game.vanta;

import com.game.vanta.net.EnableMessageHandlerScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableMessageHandlerScan(
        handlerPackages = "com.game.vanta.handler", messagePackages = "com.game.vanta.proto")
public class BootstrapApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootstrapApplication.class, args);
    }
}
