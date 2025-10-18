package com.game.vanta.redis.eventbus;

import com.game.vanta.common.codec.AbsProtostuffSchemaPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RedisEventProtostuffSchemaPool extends AbsProtostuffSchemaPool<RedisEvent>
    implements ResourceLoaderAware, ApplicationContextAware, SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(RedisEventProtostuffSchemaPool.class);

    private final String eventActionPackages;

    private ResourceLoader resourceLoader;

    private ApplicationContext applicationContext;

    private Map<Class<? extends RedisEvent>, IRedisEventAction> actionPool = new HashMap<>();

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public RedisEventProtostuffSchemaPool(String eventActionPackages) {
        this.eventActionPackages = eventActionPackages;
    }

    @Override
    public void afterSingletonsInstantiated() {

        Set<Class<?>> classes = scanClasses(eventActionPackages, Object.class);
        for (Class<?> handlerClazz : classes) {
            // handler 类必须被 Spring 管理
            if (!applicationContext.containsBeanDefinition(handlerClazz.getName())
                && applicationContext.getBeansOfType(handlerClazz).isEmpty()) {
                return;
            }
            Object bean = applicationContext.getBean(handlerClazz);
            for (Method method : handlerClazz.getDeclaredMethods()) {
                // 必须有 @MessageHandler 注解
                if (!method.isAnnotationPresent(RedisEventAction.class)) {
                    continue;
                }
                // 对比参数签名
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length != 1) {
                    continue;
                }
                Class<?> eventMessage = paramTypes[0];
                if (!RedisEvent.class.isAssignableFrom(eventMessage)) {
                    continue;
                }
                method.setAccessible(true);

                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandle mh;
                try {
                    mh = lookup.unreflect(method).bindTo(bean);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                // 接收器方法绑定 ContextHandle
                IRedisEventAction eventAction = mh::invoke;
                // 注册方法接收器 IContextHandle
                actionPool.put((Class<? extends RedisEvent>) eventMessage, eventAction);
                log.info("Registered Redis event action: {} -> {}", eventMessage, eventAction);
            }
        }

    }

    private Set<Class<?>> scanClasses(String packages, Class<?> superType) {
        Set<Class<?>> classes = new HashSet<>();
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AssignableTypeFilter(superType));
        for (BeanDefinition bd : scanner.findCandidateComponents(packages)) {
            try {
                classes.add(Class.forName(bd.getBeanClassName()));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Cannot load class " + bd.getBeanClassName(), e);
            }
        }
        return classes;
    }

    public IRedisEventAction findEventAction(Class<? extends RedisEvent> eventClazz) {
        return actionPool.get(eventClazz);
    }


}
