package org.markeb.lock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 * 用于方法级别的分布式锁
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLocked {

    /**
     * 锁的 key，支持 SpEL 表达式
     * 例如：#userId 或 'user:' + #userId
     */
    String key();

    /**
     * 等待获取锁的时间
     */
    long waitTime() default 3;

    /**
     * 持有锁的时间（自动释放）
     * -1 表示不自动释放
     */
    long leaseTime() default -1;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 获取锁失败时的错误消息
     */
    String failMessage() default "Failed to acquire lock";
}

