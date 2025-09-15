package com.game.vanta.bootstrap;

import com.game.vanta.net.EnableMessageHandlerScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@AutoConfiguration
@SpringBootApplication
@EnableMessageHandlerScan(basePackages = "com.game.vanta.bootstrap.handler")
public class BootstrapApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootstrapApplication.class, args);
    }

}
