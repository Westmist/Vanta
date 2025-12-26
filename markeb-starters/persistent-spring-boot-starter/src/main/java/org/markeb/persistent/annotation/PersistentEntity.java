package org.markeb.persistent.annotation;

import java.lang.annotation.*;

/**
 * 持久化实体注解
 * 标注在实体类上，用于配置持久化行为
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PersistentEntity {

    /**
     * 集合/表名称
     * 如果为空，则使用类名的小写形式
     */
    String collection() default "";

    /**
     * 缓存 TTL（秒）
     * 0 表示永不过期，-1 表示使用默认配置
     */
    long cacheTtl() default -1;

    /**
     * 是否启用缓存
     */
    boolean cacheEnabled() default true;

    /**
     * 是否使用异步持久化
     */
    boolean asyncPersist() default true;
}
