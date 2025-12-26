package org.markeb.eventbus.serialization;

import org.markeb.eventbus.Event;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protostuff 事件序列化实现
 */
public class ProtostuffEventSerializer implements EventSerializer {

    private static final Logger log = LoggerFactory.getLogger(ProtostuffEventSerializer.class);
    private static final ThreadLocal<LinkedBuffer> BUFFER_THREAD_LOCAL =
            ThreadLocal.withInitial(() -> LinkedBuffer.allocate(512));

    private final Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();
    private final Schema<EventWrapper> wrapperSchema = RuntimeSchema.getSchema(EventWrapper.class);

    @Override
    @SuppressWarnings("unchecked")
    public byte[] serialize(Event event) {
        // 先序列化事件本身
        Class<?> eventClass = event.getClass();
        Schema<Event> schema = (Schema<Event>) getSchema(eventClass);
        LinkedBuffer buffer = BUFFER_THREAD_LOCAL.get();

        byte[] eventBytes;
        try {
            eventBytes = ProtostuffIOUtil.toByteArray(event, schema, buffer);
        } finally {
            buffer.clear();
        }

        // 包装类型信息
        EventWrapper wrapper = new EventWrapper(eventClass.getName(), eventBytes);
        buffer = BUFFER_THREAD_LOCAL.get();
        try {
            return ProtostuffIOUtil.toByteArray(wrapper, wrapperSchema, buffer);
        } finally {
            buffer.clear();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> T deserialize(byte[] data, Class<T> clazz) {
        Schema<T> schema = (Schema<T>) getSchema(clazz);
        T event = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, event, schema);
        return event;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Event deserialize(byte[] data) {
        // 先反序列化包装器
        EventWrapper wrapper = wrapperSchema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, wrapper, wrapperSchema);

        try {
            Class<? extends Event> eventClass = (Class<? extends Event>) Class.forName(wrapper.getClassName());
            return deserialize(wrapper.getBody(), eventClass);
        } catch (ClassNotFoundException e) {
            log.error("Event class not found: {}", wrapper.getClassName(), e);
            throw new RuntimeException("Event class not found: " + wrapper.getClassName(), e);
        }
    }

    private Schema<?> getSchema(Class<?> clazz) {
        return schemaCache.computeIfAbsent(clazz, RuntimeSchema::getSchema);
    }
}

