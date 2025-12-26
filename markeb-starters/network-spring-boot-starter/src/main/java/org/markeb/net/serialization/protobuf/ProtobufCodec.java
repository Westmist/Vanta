package org.markeb.net.serialization.protobuf;

import org.markeb.net.serialization.CodecType;
import org.markeb.net.serialization.MessageCodec;
import org.markeb.net.serialization.MessageRegistry;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protobuf 编解码实现
 */
public class ProtobufCodec implements MessageCodec {

    private static final Logger log = LoggerFactory.getLogger(ProtobufCodec.class);

    private final MessageRegistry messageRegistry;
    private final Map<Class<?>, Method> parseMethodCache = new ConcurrentHashMap<>();

    public ProtobufCodec(MessageRegistry messageRegistry) {
        this.messageRegistry = messageRegistry;
    }

    @Override
    public CodecType getCodecType() {
        return CodecType.PROTOBUF;
    }

    @Override
    public byte[] encode(Object message) {
        if (message instanceof Message pbMessage) {
            return pbMessage.toByteArray();
        }
        throw new IllegalArgumentException("Message must be a Protobuf Message: " + message.getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T decode(byte[] data, Class<T> clazz) {
        try {
            Method parseFrom = getParseFromMethod(clazz);
            return (T) parseFrom.invoke(null, data);
        } catch (Exception e) {
            log.error("Failed to decode protobuf message: {}", clazz.getName(), e);
            throw new RuntimeException("Failed to decode protobuf message", e);
        }
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

    private Method getParseFromMethod(Class<?> clazz) {
        return parseMethodCache.computeIfAbsent(clazz, c -> {
            try {
                return c.getMethod("parseFrom", byte[].class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Not a valid Protobuf message class: " + c.getName(), e);
            }
        });
    }
}

