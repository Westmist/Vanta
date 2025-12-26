package org.markeb.net.annotation;

import java.lang.annotation.*;

/**
 * 消息处理器注解
 * 标注在方法上，用于自动注册消息处理器
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MsgHandler {

    /**
     * 消息ID
     */
    int value();
}

