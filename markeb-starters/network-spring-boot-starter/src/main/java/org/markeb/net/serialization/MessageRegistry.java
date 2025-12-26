package org.markeb.net.serialization;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息注册表
 * 管理消息ID与消息类型的映射
 */
public class MessageRegistry {

    private final Map<Integer, Class<?>> idToClass = new ConcurrentHashMap<>();
    private final Map<Class<?>, Integer> classToId = new ConcurrentHashMap<>();

    /**
     * 注册消息类型
     *
     * @param messageId 消息ID
     * @param clazz     消息类型
     */
    public void register(int messageId, Class<?> clazz) {
        if (idToClass.containsKey(messageId)) {
            throw new IllegalArgumentException("Message ID already registered: " + messageId);
        }
        if (classToId.containsKey(clazz)) {
            throw new IllegalArgumentException("Message class already registered: " + clazz.getName());
        }
        idToClass.put(messageId, clazz);
        classToId.put(clazz, messageId);
    }

    /**
     * 根据消息ID获取类型
     */
    public Class<?> getClass(int messageId) {
        return idToClass.get(messageId);
    }

    /**
     * 根据类型获取消息ID
     */
    public Integer getMessageId(Class<?> clazz) {
        return classToId.get(clazz);
    }

    /**
     * 是否已注册
     */
    public boolean isRegistered(int messageId) {
        return idToClass.containsKey(messageId);
    }

    /**
     * 是否已注册
     */
    public boolean isRegistered(Class<?> clazz) {
        return classToId.containsKey(clazz);
    }

    /**
     * 清空注册表
     */
    public void clear() {
        idToClass.clear();
        classToId.clear();
    }
}

