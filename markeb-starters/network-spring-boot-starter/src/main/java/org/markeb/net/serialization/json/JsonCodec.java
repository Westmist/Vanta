package org.markeb.net.serialization.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.markeb.net.serialization.CodecType;
import org.markeb.net.serialization.MessageCodec;
import org.markeb.net.serialization.MessageRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON 编解码实现
 * 使用 Jackson 进行序列化
 */
public class JsonCodec implements MessageCodec {

    private static final Logger log = LoggerFactory.getLogger(JsonCodec.class);

    private final MessageRegistry messageRegistry;
    private final ObjectMapper objectMapper;

    public JsonCodec(MessageRegistry messageRegistry) {
        this(messageRegistry, new ObjectMapper());
    }

    public JsonCodec(MessageRegistry messageRegistry, ObjectMapper objectMapper) {
        this.messageRegistry = messageRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    public CodecType getCodecType() {
        return CodecType.JSON;
    }

    @Override
    public byte[] encode(Object message) {
        try {
            return objectMapper.writeValueAsBytes(message);
        } catch (Exception e) {
            log.error("Failed to encode JSON message: {}", message.getClass().getName(), e);
            throw new RuntimeException("Failed to encode JSON message", e);
        }
    }

    @Override
    public <T> T decode(byte[] data, Class<T> clazz) {
        try {
            return objectMapper.readValue(data, clazz);
        } catch (Exception e) {
            log.error("Failed to decode JSON message: {}", clazz.getName(), e);
            throw new RuntimeException("Failed to decode JSON message", e);
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
}

