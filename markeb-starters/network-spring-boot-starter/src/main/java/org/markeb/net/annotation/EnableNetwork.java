package org.markeb.net.annotation;

import org.markeb.net.config.NetworkAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用网络模块
 * 在 Spring Boot 启动类上添加此注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(NetworkAutoConfiguration.class)
public @interface EnableNetwork {

    /**
     * 消息处理器扫描包路径
     */
    String[] handlerPackages() default {};

    /**
     * 消息类扫描包路径
     */
    String[] messagePackages() default {};
}

