package com.game.vanta.net.register;

import com.game.vanta.net.EnableMessageHandlerScan;
import com.game.vanta.net.netty.NettyServer;
import com.game.vanta.net.msg.IGameParser;
import com.game.vanta.net.msg.IMessagePool;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.lang.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class MessageHandlerRegistrar implements SmartInitializingSingleton,
        ResourceLoaderAware, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    private ResourceLoader resourceLoader;

    private ApplicationContext applicationContext;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterSingletonsInstantiated() {
        EnableMessageHandlerScan scanAnno = findScanAnno();
        if (scanAnno == null) {
            return;
        }
        IMessagePool<?> messagePool = applicationContext.getBean(IMessagePool.class);
        IGameParser<?> parser = messagePool.messageParser();
        Class<?> defaultMessageClazz = parser.messageClazz();

        // 注册消息池
        String[] messagePackages = scanAnno.messagePackages();
        for (Class messageClazz : scanClasses(messagePackages, defaultMessageClazz)) {
            parser.register(messageClazz);
        }

        // 注册处理池
        String[] handlerPackages = scanAnno.handlerPackages();
        for (Class<?> handlerClazz : scanClasses(handlerPackages)) {
            // handler 类必须被 Spring 管理
            if (!applicationContext.containsBeanDefinition(handlerClazz.getName())
                    && applicationContext.getBeansOfType(handlerClazz).isEmpty()) {
                return;
            }
            Object bean = applicationContext.getBean(handlerClazz);
            for (Method method : handlerClazz.getDeclaredMethods()) {
                // 过滤出满足条件的消息接收器
                if (!isHandleMethod(method, defaultMessageClazz)) {
                    continue;
                }
                method.setAccessible(true);

                Class<?>[] paramTypes = method.getParameterTypes();
                Class messageClazz = paramTypes[1];
                int msgId = parser.messageId(messageClazz);

                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodHandle mh;
                try {
                    mh = lookup.unreflect(method).bindTo(bean);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                // 接收器方法绑定 ContextHandle
                IContextHandle contextHandle = method.getReturnType() == void.class ?
                        (ctx, req) -> {
                            mh.invoke(ctx, req);
                            return null;
                        } : mh::invoke;
                // 注册方法接收器 IContextHandle
                messagePool.register(msgId, contextHandle);
                log.info("Registering handler, msgId: {}, className : {}, Method: {}", msgId, handlerClazz.getName(), method.getName());
            }
        }
    }

    @Nullable
    private EnableMessageHandlerScan findScanAnno() {
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(SpringBootApplication.class);
        if (beanNames.length == 0) {
            throw new IllegalStateException("No @SpringBootApplication found in context");
        }
        Object mainBean = applicationContext.getBean(beanNames[0]);
        Class<?> mainClass = AopUtils.getTargetClass(mainBean);
        EnableMessageHandlerScan anno = mainClass.getAnnotation(EnableMessageHandlerScan.class);
        if (anno == null) {
            log.debug("No @EnableMessageHandlerScan found on @SpringBootApplication class: {}", mainClass.getName());
            return null;
        }
        return anno;
    }

    private Set<Class<?>> scanClasses(String[] packages) {
        return scanClasses(packages, Object.class);
    }

    private Set<Class<?>> scanClasses(String[] packages, Class<?> superType) {
        Set<Class<?>> classes = new HashSet<>();
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AssignableTypeFilter(superType));
        for (String packageName : packages) {
            for (BeanDefinition bd : scanner.findCandidateComponents(packageName)) {
                try {
                    classes.add(Class.forName(bd.getBeanClassName()));
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Cannot load class " + bd.getBeanClassName(), e);
                }
            }
        }
        return classes;
    }

    private boolean isHandleMethod(Method method, Class<?> defaultMessageClazz) {
        // 必须有 @MessageHandler 注解
        if (!method.isAnnotationPresent(MessageHandler.class)) {
            return false;
        }
        // 必须是 public
        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }

        // 找出 IContextHandle 中唯一的抽象方法（函数式接口保证只有一个）
        Method ifaceMethod = null;
        for (Method m : IContextHandle.class.getMethods()) {
            if (Modifier.isAbstract(m.getModifiers())) {
                ifaceMethod = m;
                break;
            }
        }
        if (ifaceMethod == null) {
            return false;
        }

        // 对比参数签名
        Class<?>[] ifaceParamTypes = ifaceMethod.getParameterTypes();
        Class<?>[] paramTypes = method.getParameterTypes();

        if (paramTypes.length != ifaceParamTypes.length) {
            return false;
        }

        // 第一个参数必须兼容 GameActorContext
        if (!GameActorContext.class.isAssignableFrom(paramTypes[0])) {
            return false;
        }

        // 第二个参数必须兼容 defaultMessageClazz
        if (!defaultMessageClazz.isAssignableFrom(paramTypes[1])) {
            return false;
        }

        // 返回值必须和第二个参数类型兼容或者是 void
        if (!defaultMessageClazz.isAssignableFrom(method.getReturnType())
                && method.getReturnType() != void.class) {
            return false;
        }

        return true;
    }

}
