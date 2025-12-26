package org.markeb.eventbus.serialization;

import lombok.Data;

import java.io.Serializable;

/**
 * 事件包装器
 * 用于序列化时携带类型信息
 */
@Data
public class EventWrapper implements Serializable {

    /**
     * 事件类名
     */
    private String className;

    /**
     * 事件体（序列化后的字节数组）
     */
    private byte[] body;

    public EventWrapper() {
    }

    public EventWrapper(String className, byte[] body) {
        this.className = className;
        this.body = body;
    }
}

