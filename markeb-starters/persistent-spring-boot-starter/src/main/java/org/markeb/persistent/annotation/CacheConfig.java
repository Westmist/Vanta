package org.markeb.persistent.annotation;

import java.lang.annotation.*;

/**
 * 缓存配置注解
 * 用于自定义实体的缓存策略
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheConfig {

    /**
     * 缓存键前缀
     */
    String keyPrefix() default "";

    /**
     * 本地缓存过期时间（秒）
     */
    long localTtlSeconds() default 300;

    /**
     * 远程缓存过期时间（秒）
     */
    long remoteTtlSeconds() default 1800;

    /**
     * 是否使用两级缓存
     */
    boolean twoLevel() default false;

}

