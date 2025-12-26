package org.markeb.persistent.serialization;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protostuff 实体序列化实现
 */
public class ProtostuffEntitySerializer implements EntitySerializer {

    private static final Logger log = LoggerFactory.getLogger(ProtostuffEntitySerializer.class);
    private static final ThreadLocal<LinkedBuffer> BUFFER_THREAD_LOCAL =
            ThreadLocal.withInitial(() -> LinkedBuffer.allocate(512));

    private final Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public byte[] serialize(Object entity) {
        Class<?> clazz = entity.getClass();
        Schema<Object> schema = (Schema<Object>) getSchema(clazz);
        LinkedBuffer buffer = BUFFER_THREAD_LOCAL.get();
        try {
            return ProtostuffIOUtil.toByteArray(entity, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        Schema<T> schema = (Schema<T>) getSchema(clazz);
        T entity = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, entity, schema);
        return entity;
    }

    private Schema<?> getSchema(Class<?> clazz) {
        return schemaCache.computeIfAbsent(clazz, RuntimeSchema::getSchema);
    }
}

