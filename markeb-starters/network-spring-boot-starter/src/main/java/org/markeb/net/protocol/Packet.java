package org.markeb.net.protocol;

/**
 * 通用数据包接口
 * 支持网关协议和游戏服协议
 */
public interface Packet {

    /**
     * 获取消息ID
     */
    int getMessageId();

    /**
     * 获取序列号
     */
    short getSeq();

    /**
     * 获取消息体字节数组
     */
    byte[] getBody();

    /**
     * 获取协议类型
     */
    ProtocolType getProtocolType();
}

