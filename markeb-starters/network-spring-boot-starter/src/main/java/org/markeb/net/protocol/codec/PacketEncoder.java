package org.markeb.net.protocol.codec;

import org.markeb.net.protocol.GameServerPacket;
import org.markeb.net.protocol.GatewayPacket;
import org.markeb.net.protocol.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 协议编码器
 * 根据 Packet 类型编码为对应的字节流
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) {
        switch (msg) {
            case GatewayPacket gp -> encodeGatewayPacket(gp, out);
            case GameServerPacket gsp -> encodeGameServerPacket(gsp, out);
            default -> throw new IllegalArgumentException("Unknown packet type: " + msg.getClass());
        }
    }

    /**
     * 编码网关协议
     * 4 length + 4 messageId + 2 seq + 2 magicNum + body
     */
    private void encodeGatewayPacket(GatewayPacket packet, ByteBuf out) {
        byte[] body = packet.getBody();
        int bodyLength = body != null ? body.length : 0;
        int totalLength = 12 + bodyLength; // 4 + 4 + 2 + 2 + body

        out.writeInt(totalLength);
        out.writeInt(packet.getMessageId());
        out.writeShort(packet.getSeq());
        out.writeShort(packet.getMagicNum());
        if (body != null && bodyLength > 0) {
            out.writeBytes(body);
        }
    }

    /**
     * 编码游戏服协议
     * 4 length + 4 messageId + 2 seq + 2 gateId + 8 roleId + 8 conId + body
     */
    private void encodeGameServerPacket(GameServerPacket packet, ByteBuf out) {
        byte[] body = packet.getBody();
        int bodyLength = body != null ? body.length : 0;
        int totalLength = 28 + bodyLength; // 4 + 4 + 2 + 2 + 8 + 8 + body

        out.writeInt(totalLength);
        out.writeInt(packet.getMessageId());
        out.writeShort(packet.getSeq());
        out.writeShort(packet.getGateId());
        out.writeLong(packet.getRoleId());
        out.writeLong(packet.getConId());
        if (body != null && bodyLength > 0) {
            out.writeBytes(body);
        }
    }
}

