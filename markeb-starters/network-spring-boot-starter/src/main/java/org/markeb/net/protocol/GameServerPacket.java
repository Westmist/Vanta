package org.markeb.net.protocol;

import lombok.Getter;
import lombok.Setter;

/**
 * 游戏服协议数据包
 * 协议格式: 4 length + 4 messageId + 2 seq + 2 gateId + 8 roleId + 8 conId + body
 */
@Getter
@Setter
public class GameServerPacket implements Packet {

    /**
     * 消息ID
     */
    private int messageId;

    /**
     * 序列号
     */
    private short seq;

    /**
     * 网关ID
     */
    private short gateId;

    /**
     * 角色ID
     */
    private long roleId;

    /**
     * 连接ID
     */
    private long conId;

    /**
     * 消息体
     */
    private byte[] body;

    public GameServerPacket() {
    }

    public GameServerPacket(int messageId, short seq, short gateId, long roleId, long conId, byte[] body) {
        this.messageId = messageId;
        this.seq = seq;
        this.gateId = gateId;
        this.roleId = roleId;
        this.conId = conId;
        this.body = body;
    }

    @Override
    public ProtocolType getProtocolType() {
        return ProtocolType.GAME_SERVER;
    }

    /**
     * 计算总长度（包含长度字段本身）
     */
    public int getTotalLength() {
        return 28 + (body != null ? body.length : 0);
    }

    /**
     * 从网关包转换（网关转发到游戏服时使用）
     */
    public static GameServerPacket fromGatewayPacket(GatewayPacket gatewayPacket, 
                                                      short gateId, long roleId, long conId) {
        return new GameServerPacket(
                gatewayPacket.getMessageId(),
                gatewayPacket.getSeq(),
                gateId,
                roleId,
                conId,
                gatewayPacket.getBody()
        );
    }

    /**
     * 转换为网关包（游戏服回包到网关时使用）
     */
    public GatewayPacket toGatewayPacket() {
        return new GatewayPacket(messageId, seq, body);
    }
}

