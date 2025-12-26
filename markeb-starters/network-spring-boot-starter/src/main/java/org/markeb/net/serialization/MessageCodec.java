package org.markeb.net.serialization;

/**
 * 消息编解码接口
 */
public interface MessageCodec {

    /**
     * 获取编解码类型
     */
    CodecType getCodecType();

    /**
     * 序列化消息
     *
     * @param message 消息对象
     * @return 字节数组
     */
    byte[] encode(Object message);

    /**
     * 反序列化消息
     *
     * @param data  字节数组
     * @param clazz 目标类型
     * @return 消息对象
     */
    <T> T decode(byte[] data, Class<T> clazz);

    /**
     * 根据消息ID反序列化
     *
     * @param messageId 消息ID
     * @param data      字节数组
     * @return 消息对象
     */
    Object decode(int messageId, byte[] data);
}

