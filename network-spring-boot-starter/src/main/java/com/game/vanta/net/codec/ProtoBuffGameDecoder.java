package com.game.vanta.net.codec;

import com.game.vanta.net.msg.IGameParser;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 消息解码器 消息结构 +----------+ | 总长度 | +----------+ | 消息ID | +----------+ | 主体数据 | +----------+ 总长度 =
 * 4(长度标示) + 4(消息ID) + 主体数据
 */
public class ProtoBuffGameDecoder extends LengthFieldBasedFrameDecoder {

    private final IGameParser<Message> parser;

    public ProtoBuffGameDecoder(IGameParser<Message> parser) {
        super(1024 * 1024, 0, 4, -4, 4);
        this.parser = parser;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        try {
            int msgId = frame.readInt();
            byte[] bodyBytes = new byte[frame.readableBytes()];
            frame.readBytes(bodyBytes);
            return parser.parseFrom(msgId, bodyBytes);
        } finally {
            frame.release();
        }
    }
}
