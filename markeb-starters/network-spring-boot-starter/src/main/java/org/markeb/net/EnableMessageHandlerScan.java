package org.markeb.net;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(NetworkAutoConfiguration.class)
public @interface EnableMessageHandlerScan {

    String[] messagePackages();

    String[] handlerPackages();

}
