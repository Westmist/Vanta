package com.game.vanta.net.register;

import com.game.vanta.net.EnableMessageHandlerScan;
import com.game.vanta.net.NettyServer;
import com.game.vanta.net.msg.IGameParser;
import com.game.vanta.net.msg.IMessagePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

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
    public void afterSingletonsInstantiated() {
        IMessagePool<?> messagePool = applicationContext.getBean(IMessagePool.class);

        String[] basePackages = null;
        // 找到标注了 @EnableMessageHandlerScan 的原始类
        Map<String, Object> configs = applicationContext.getBeansWithAnnotation(EnableMessageHandlerScan.class);
        for (Object config : configs.values()) {
            // 防止 CGLIB 代理
            Class<?> targetClass = AopUtils.getTargetClass(config);
            EnableMessageHandlerScan anno = targetClass.getAnnotation(EnableMessageHandlerScan.class);
            if (anno != null) {
                basePackages = anno.basePackages();
                break;
            }
        }
        if (basePackages == null || basePackages.length == 0) {
            log.warn("No basePackages found for MessageHandler scan");
            return;
        }
        scanAndRegister(basePackages, messagePool);
    }

    private void scanAndRegister(String[] basePackages, IMessagePool<?> messagePool) {
        // 1. 扫描指定包下的所有类
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.setResourceLoader(resourceLoader);
        scanner.addIncludeFilter(new AssignableTypeFilter(Object.class));

        IGameParser<?> iGameParser = messagePool.messageParser();
        Class<?> defaultMessageClazz = iGameParser.messageClazz();

        // 2. 注册满足条件的消息接收器
        for (String basePackage : basePackages) {
            scanner.findCandidateComponents(basePackage).forEach(candidate -> {
                try {
                    Class<?> clazz = Class.forName(candidate.getBeanClassName());

                    if (!applicationContext.containsBeanDefinition(clazz.getName())
                            && applicationContext.getBeansOfType(clazz).isEmpty()) {
                        return;
                    }

                    Object bean = applicationContext.getBean(clazz);

                    for (Method method : clazz.getDeclaredMethods()) {
                        // 过滤出满足条件的消息接收器
                        if (!isHandleMethod(method, defaultMessageClazz)) {
                            continue;
                        }
                        Class<?>[] paramTypes = method.getParameterTypes();
                        Class messageClazz = paramTypes[1];
                        // 注册协议解析器
                        iGameParser.register(messageClazz);

                        int msgId = iGameParser.messageId(messageClazz);
                        log.info("Registered message: {} with msgId: {}", messageClazz.getName(), msgId);

                        method.setAccessible(true);
                        MethodHandles.Lookup lookup = MethodHandles.lookup();
                        MethodHandle mh = lookup.unreflect(method).bindTo(bean);
                        // 接收器方法绑定 ContextHandle
                        IContextHandle contextHandle = method.getReturnType() == void.class ?
                                (ctx, req) -> {
                                    mh.invoke(ctx, req);
                                    return null;
                                } : mh::invoke;
                        // 注册方法接收器 IContextHandle
                        messagePool.register(msgId, contextHandle);
                        log.info("Registering handler, msgId: {}, className : {}, Method: {}", msgId, clazz.getName(), method.getName());
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to scan handler: " + candidate.getBeanClassName(), e);
                }
            });
        }
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
