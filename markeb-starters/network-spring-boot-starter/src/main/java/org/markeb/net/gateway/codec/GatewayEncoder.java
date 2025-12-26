package org.markeb.net.gateway.codec;

import org.markeb.net.gateway.GatewayPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 网关协议编码器。
 * <p>
 * 网关 -> 客户端协议头： length(4) + msgId(4) + seq(4) + body(n)
 * 网关 -> 游戏服协议头： length(4) + sessionId(4) + msgId(4) + seq(4) + body(n)
 * <p>
 * 通过 forFrontend 参数区分：
 * - forFrontend=true: 编码客户端协议（不写 sessionId）
 * - forFrontend=false: 编码内部协议（写 sessionId）
 */
public class GatewayEncoder extends MessageToByteEncoder<GatewayPacket> {

    private final boolean forFrontend;

    /**
     * 默认用于前端连接（客户端协议）
     */
    public GatewayEncoder() {
        this(true);
    }

    /**
     * @param forFrontend true=编码客户端协议（无sessionId），false=编码内部协议（有sessionId）
     */
    public GatewayEncoder(boolean forFrontend) {
        this.forFrontend = forFrontend;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, GatewayPacket packet, ByteBuf out) {
        byte[] body = packet.getBody();
        int bodyLen = body == null ? 0 : body.length;

        if (forFrontend) {
            // 客户端协议：length + msgId + seq + body
            out.writeInt(8 + bodyLen);
            out.writeInt(packet.getMsgId());
            out.writeInt(packet.getSeq());
        } else {
            // 内部协议：length + sessionId + msgId + seq + body
            out.writeInt(12 + bodyLen);
            out.writeInt(packet.getSessionId());
            out.writeInt(packet.getMsgId());
            out.writeInt(packet.getSeq());
        }

        if (bodyLen > 0) {
            out.writeBytes(body);
        }
    }
}

