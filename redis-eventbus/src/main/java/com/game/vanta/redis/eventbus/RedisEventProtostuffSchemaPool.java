package com.game.vanta.redis.eventbus;

import com.game.vanta.common.codec.AbsProtostuffSchemaPool;
import com.game.vanta.common.scanner.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RedisEventProtostuffSchemaPool extends AbsProtostuffSchemaPool<RedisEvent>
    implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(RedisEventProtostuffSchemaPool.class);

    private final String eventActionPackages;

    private final ApplicationContext applicationContext;

    private Map<Class<? extends RedisEvent>, IRedisEventAction> actionPool = new HashMap<>();

    public RedisEventProtostuffSchemaPool(
        String eventActionPackages,
        ApplicationContext applicationContext) {
        this.eventActionPackages = eventActionPackages;
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {

        Set<Class<?>> eventActionClasses = ClassScanner.builder()
            .basePackages(eventActionPackages)
            .onlySpringBeans(applicationContext)
            .scan();
        for (Class<?> handlerClazz : eventActionClasses) {
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
                MethodHandle mh = ClassScanner.bindBean(bean, method);
                actionPool.put((Class<? extends RedisEvent>) eventMessage, mh::invoke);
                log.info("Registered Redis event action: {}, Method: {}", eventMessage, method.getName());
            }
        }

    }

    public IRedisEventAction findEventAction(Class<? extends RedisEvent> eventClazz) {
        return actionPool.get(eventClazz);
    }

}
