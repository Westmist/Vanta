package org.markeb.eventbus.config;

import org.markeb.eventbus.Event;
import org.markeb.eventbus.EventHandler;
import org.markeb.eventbus.EventSubscriber;
import org.markeb.eventbus.annotation.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 事件监听器扫描器
 * 自动扫描并注册标注了 @EventListener 的方法
 */
@Component
public class EventListenerScanner implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(EventListenerScanner.class);

    private final ApplicationContext applicationContext;
    private final EventSubscriber eventSubscriber;
    private final EventBusProperties properties;

    public EventListenerScanner(ApplicationContext applicationContext,
                                 EventSubscriber eventSubscriber,
                                 EventBusProperties properties) {
        this.applicationContext = applicationContext;
        this.eventSubscriber = eventSubscriber;
        this.properties = properties;
    }

    @Override
    public void afterSingletonsInstantiated() {
        // 扫描所有 Spring Bean
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = AopUtils.getTargetClass(bean);

            // 扫描方法
            ReflectionUtils.doWithMethods(beanClass, method -> {
                EventListener annotation = AnnotationUtils.findAnnotation(method, EventListener.class);
                if (annotation != null) {
                    registerEventListener(bean, method, annotation);
                }
            }, method -> Modifier.isPublic(method.getModifiers()));
        }

        // 启动订阅者
        eventSubscriber.start();
    }

    @SuppressWarnings("unchecked")
    private void registerEventListener(Object bean, Method method, EventListener annotation) {
        // 验证方法签名
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != 1) {
            log.warn("Invalid event listener method signature: {}.{}, expected 1 parameter",
                    bean.getClass().getSimpleName(), method.getName());
            return;
        }

        if (!Event.class.isAssignableFrom(paramTypes[0])) {
            log.warn("Parameter must be Event type: {}.{}",
                    bean.getClass().getSimpleName(), method.getName());
            return;
        }

        Class<? extends Event> eventType = (Class<? extends Event>) paramTypes[0];
        String topic = annotation.topic();

        // 如果没有指定 topic，尝试从事件实例获取
        if (topic.isEmpty()) {
            try {
                Event instance = eventType.getDeclaredConstructor().newInstance();
                topic = instance.topic();
            } catch (Exception e) {
                log.warn("Failed to get topic from event type: {}, please specify topic in @EventListener",
                        eventType.getName());
                return;
            }
        }

        // 创建处理器
        EventHandler<Event> handler = event -> {
            try {
                method.invoke(bean, event);
            } catch (Exception e) {
                throw new RuntimeException("Failed to invoke event listener: " + method.getName(), e);
            }
        };

        eventSubscriber.subscribe(topic, handler);
        log.info("Registered event listener: {}.{} for topic: {}",
                bean.getClass().getSimpleName(), method.getName(), topic);
    }
}

