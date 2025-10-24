package com.game.vanta.common.codec;


import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeEnv;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbsProtostuffSchemaPool<T> {

    private final Map<Class<T>, Schema<T>> CACHED_SCHEMA = new ConcurrentHashMap<>();

    private Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = CACHED_SCHEMA.get(clazz);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(clazz, RuntimeEnv.ID_STRATEGY);
            CACHED_SCHEMA.put(clazz, schema);
        }
        return schema;
    }

    /**
     * 序列化（对象 -> 字节数组）
     * exclusions 屏蔽序列化的字段
     */
    @SuppressWarnings("unchecked")
    public byte[] serialize(T obj) {
        Class<T> clazz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(clazz);
            return ProtobufIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化（字节数组 -> 对象）
     * exclusions 屏蔽序列化的字段
     */
    public T deserialize(byte[] data, T message) {
        try {
            Class<T> cls = (Class<T>) message.getClass();
            Schema<T> schema = getSchema(cls);
            ProtobufIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
