package com.game.vanta.net;

import java.lang.annotation.*;
import org.springframework.context.annotation.Import;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(NetworkAutoConfiguration.class)
public @interface EnableMessageHandlerScan {

    String[] messagePackages();

    String[] handlerPackages();
}
