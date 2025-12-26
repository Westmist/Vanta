package org.markeb.net.transport.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

/**
 * WebSocket 数据包编码器
 * 将 ByteBuf 包装为 WebSocket 二进制帧
 */
public class WebSocketPacketEncoder extends MessageToMessageEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        // 将 ByteBuf 包装为 WebSocket 二进制帧
        out.add(new BinaryWebSocketFrame(msg.retain()));
    }
}

