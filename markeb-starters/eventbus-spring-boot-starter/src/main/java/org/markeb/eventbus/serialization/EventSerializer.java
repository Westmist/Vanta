package org.markeb.eventbus.serialization;

import org.markeb.eventbus.Event;

/**
 * 事件序列化接口
 */
public interface EventSerializer {

    /**
     * 序列化事件
     *
     * @param event 事件
     * @return 字节数组
     */
    byte[] serialize(Event event);

    /**
     * 反序列化事件
     *
     * @param data  字节数组
     * @param clazz 事件类型
     * @return 事件
     */
    <T extends Event> T deserialize(byte[] data, Class<T> clazz);

    /**
     * 反序列化事件（自动识别类型）
     *
     * @param data 包含类型信息的字节数组
     * @return 事件
     */
    Event deserialize(byte[] data);
}

