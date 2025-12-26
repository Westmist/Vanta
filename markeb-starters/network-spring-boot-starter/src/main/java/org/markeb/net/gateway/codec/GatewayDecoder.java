package org.markeb.net.gateway.codec;

import org.markeb.net.gateway.GatewayPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 网关协议解码器。
 * <p>
 * 客户端 -> 网关协议头： length(4) + msgId(4) + seq(4) + body(n)
 * 网关 <-> 游戏服协议头： length(4) + sessionId(4) + msgId(4) + seq(4) + body(n)
 * <p>
 * 通过 forFrontend 参数区分：
 * - forFrontend=true: 解析客户端协议（无 sessionId），sessionId 由外部填充
 * - forFrontend=false: 解析内部协议（有 sessionId）
 */
public class GatewayDecoder extends LengthFieldBasedFrameDecoder {

    private static final int MAX_FRAME_LENGTH = 1024 * 1024;

    private final boolean forFrontend;

    /**
     * 默认用于前端连接（客户端协议）
     */
    public GatewayDecoder() {
        this(true);
    }

    /**
     * @param forFrontend true=解析客户端协议（无sessionId），false=解析内部协议（有sessionId）
     */
    public GatewayDecoder(boolean forFrontend) {
        super(MAX_FRAME_LENGTH,
            0,      // lengthFieldOffset
            4,      // lengthFieldLength
            0,      // lengthAdjustment
            4);     // initialBytesToStrip (strip length field)
        this.forFrontend = forFrontend;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        try {
            int sessionId;
            if (forFrontend) {
                // 客户端协议没有 sessionId，先填 0，后续由 handler 填充
                sessionId = 0;
            } else {
                // 内部协议有 sessionId
                sessionId = frame.readInt();
            }
            int msgId = frame.readInt();
            int seq = frame.readInt();
            byte[] body = new byte[frame.readableBytes()];
            frame.readBytes(body);
            return new GatewayPacket(sessionId, msgId, seq, body);
        } finally {
            frame.release();
        }
    }
}

