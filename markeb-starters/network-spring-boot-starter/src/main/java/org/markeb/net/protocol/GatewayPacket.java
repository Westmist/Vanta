package org.markeb.net.protocol;

import lombok.Getter;
import lombok.Setter;

/**
 * 网关协议数据包
 * 协议格式: 4 length + 4 messageId + 2 seq + 2 magicNum + body
 */
@Getter
@Setter
public class GatewayPacket implements Packet {

    public static final short DEFAULT_MAGIC = (short) 0xABCD;

    /**
     * 消息ID
     */
    private int messageId;

    /**
     * 序列号
     */
    private short seq;

    /**
     * 魔数（用于校验）
     */
    private short magicNum;

    /**
     * 消息体
     */
    private byte[] body;

    public GatewayPacket() {
        this.magicNum = DEFAULT_MAGIC;
    }

    public GatewayPacket(int messageId, short seq, byte[] body) {
        this.messageId = messageId;
        this.seq = seq;
        this.magicNum = DEFAULT_MAGIC;
        this.body = body;
    }

    public GatewayPacket(int messageId, short seq, short magicNum, byte[] body) {
        this.messageId = messageId;
        this.seq = seq;
        this.magicNum = magicNum;
        this.body = body;
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.GATEWAY;
    }

    /**
     * 计算总长度（包含长度字段本身）
     */
    public int getTotalLength() {
        return 12 + (body != null ? body.length : 0);
    }
}

