package org.markeb.net.handler;

import org.markeb.net.annotation.EnableNetwork;
import org.markeb.net.annotation.MsgHandler;
import org.markeb.net.serialization.MessageRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 消息处理器扫描器
 * 自动扫描并注册标注了 @MsgHandler 的方法
 */
@Component
public class MessageHandlerScanner implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(MessageHandlerScanner.class);

    private final ApplicationContext applicationContext;
    private final MessageDispatcher messageDispatcher;
    private final MessageRegistry messageRegistry;

    public MessageHandlerScanner(ApplicationContext applicationContext,
                                  MessageDispatcher messageDispatcher,
                                  MessageRegistry messageRegistry) {
        this.applicationContext = applicationContext;
        this.messageDispatcher = messageDispatcher;
        this.messageRegistry = messageRegistry;
    }

    @Override
    public void afterSingletonsInstantiated() {
        EnableNetwork enableNetwork = findEnableNetwork();
        if (enableNetwork == null) {
            log.info("@EnableNetwork not found, skipping message handler scanning");
            return;
        }

        // 扫描所有 Spring Bean
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = AopUtils.getTargetClass(bean);

            // 扫描方法
            ReflectionUtils.doWithMethods(beanClass, method -> {
                MsgHandler annotation = AnnotationUtils.findAnnotation(method, MsgHandler.class);
                if (annotation != null) {
                    registerHandler(bean, method, annotation.value());
                }
            }, method -> Modifier.isPublic(method.getModifiers()));
        }
    }

    private EnableNetwork findEnableNetwork() {
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(SpringBootApplication.class);
        if (beanNames.length == 0) {
            return null;
        }
        Object mainBean = applicationContext.getBean(beanNames[0]);
        Class<?> mainClass = AopUtils.getTargetClass(mainBean);
        return AnnotationUtils.findAnnotation(mainClass, EnableNetwork.class);
    }

    private void registerHandler(Object bean, Method method, int messageId) {
        // 验证方法签名
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != 2) {
            log.warn("Invalid handler method signature: {}.{}, expected 2 parameters",
                    bean.getClass().getSimpleName(), method.getName());
            return;
        }

        if (!MessageContext.class.isAssignableFrom(paramTypes[0])) {
            log.warn("First parameter must be MessageContext: {}.{}",
                    bean.getClass().getSimpleName(), method.getName());
            return;
        }

        // 注册消息类型
        Class<?> messageType = paramTypes[1];
        if (!messageRegistry.isRegistered(messageId)) {
            messageRegistry.register(messageId, messageType);
        }

        // 创建处理器
        MessageHandler<Object> handler = (context, message) -> {
            try {
                return method.invoke(bean, context, message);
            } catch (Exception e) {
                throw new RuntimeException("Failed to invoke handler: " + method.getName(), e);
            }
        };

        messageDispatcher.registerHandler(messageId, handler);
        log.info("Registered handler: {}.{} for messageId: {}",
                bean.getClass().getSimpleName(), method.getName(), messageId);
    }
}

