package org.markeb.net.serialization.protostuff;

import org.markeb.net.serialization.CodecType;
import org.markeb.net.serialization.MessageCodec;
import org.markeb.net.serialization.MessageRegistry;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protostuff 编解码实现
 * 比 Protobuf 更快，且不需要 .proto 文件
 */
public class ProtostuffCodec implements MessageCodec {

    private static final Logger log = LoggerFactory.getLogger(ProtostuffCodec.class);
    private static final ThreadLocal<LinkedBuffer> BUFFER_THREAD_LOCAL =
            ThreadLocal.withInitial(() -> LinkedBuffer.allocate(512));

    private final MessageRegistry messageRegistry;
    private final Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

    public ProtostuffCodec(MessageRegistry messageRegistry) {
        this.messageRegistry = messageRegistry;
    }

    @Override
    public CodecType getCodecType() {
        return CodecType.PROTOSTUFF;
    }

    @Override
    @SuppressWarnings("unchecked")
    public byte[] encode(Object message) {
        Class<?> clazz = message.getClass();
        Schema<Object> schema = (Schema<Object>) getSchema(clazz);
        LinkedBuffer buffer = BUFFER_THREAD_LOCAL.get();
        try {
            return ProtostuffIOUtil.toByteArray(message, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T decode(byte[] data, Class<T> clazz) {
        Schema<T> schema = (Schema<T>) getSchema(clazz);
        T message = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, message, schema);
        return message;
    }

    @Override
    public Object decode(int messageId, byte[] data) {
        Class<?> clazz = messageRegistry.getClass(messageId);
        if (clazz == null) {
            log.warn("Unknown message ID: {}", messageId);
            return null;
        }
        return decode(data, clazz);
    }

    private Schema<?> getSchema(Class<?> clazz) {
        return schemaCache.computeIfAbsent(clazz, RuntimeSchema::getSchema);
    }
}

