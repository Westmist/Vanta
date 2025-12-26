package org.markeb.eventbus.annotation;

import java.lang.annotation.*;

/**
 * 事件监听器注解
 * 标注在方法上，用于自动注册事件处理器
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventListener {

    /**
     * 监听的主题
     * 如果为空，则使用事件的 topic() 方法返回值
     */
    String topic() default "";

    /**
     * 标签（仅 RocketMQ 有效）
     */
    String tag() default "*";
}

