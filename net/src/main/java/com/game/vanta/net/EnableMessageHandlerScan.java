package com.game.vanta.net;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(NettyAutoConfiguration.class)
public @interface EnableMessageHandlerScan {
    String[] basePackages() default {};
}
