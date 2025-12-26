package org.markeb.persistent.serialization;

/**
 * 实体序列化接口
 */
public interface EntitySerializer {

    /**
     * 序列化实体
     *
     * @param entity 实体
     * @return 字节数组
     */
    byte[] serialize(Object entity);

    /**
     * 反序列化实体
     *
     * @param data  字节数组
     * @param clazz 实体类型
     * @return 实体
     */
    <T> T deserialize(byte[] data, Class<T> clazz);
}

