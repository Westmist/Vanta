package org.markeb.net.serialization;

/**
 * 编解码类型
 */
public enum CodecType {

    /**
     * Google Protobuf
     */
    PROTOBUF,

    /**
     * Protostuff（更快的 Protobuf 兼容序列化）
     */
    PROTOSTUFF,

    /**
     * JSON
     */
    JSON
}

