package org.markeb.actor.config;

import org.markeb.actor.ActorSystem;
import org.markeb.actor.impl.DefaultActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Actor 模块自动配置
 * <p>
 * 遵循约定大于配置原则：
 * <ul>
 *   <li>默认启用，无需任何配置</li>
 *   <li>系统名称自动从 spring.application.name 获取</li>
 *   <li>默认使用虚拟线程执行器</li>
 * </ul>
 * </p>
 */
@AutoConfiguration
@EnableConfigurationProperties(ActorProperties.class)
@ConditionalOnProperty(prefix = "markeb.actor", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ActorAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ActorAutoConfiguration.class);

    /**
     * 创建 ActorSystem
     * <p>
     * 系统名称优先级：
     * 1. markeb.actor.system-name 配置
     * 2. spring.application.name 配置
     * 3. 默认值 "markeb"
     * </p>
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public ActorSystem actorSystem(
            ActorProperties properties,
            @Value("${spring.application.name:}") String applicationName) {

        String systemName = properties.resolveSystemName(applicationName);

        log.info("Creating ActorSystem '{}' [executor={}, parallelism={}]",
                systemName,
                properties.getExecutorType(),
                properties.getExecutorType() == org.markeb.actor.ExecutorType.PLATFORM
                        ? properties.resolveParallelism()
                        : "N/A");

        return new DefaultActorSystem(systemName, properties);
    }

}
