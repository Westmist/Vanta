package org.markeb.net.transport.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket 帧处理器
 * 将 WebSocket 帧转换为 ByteBuf 传递给后续的协议解码器
 */
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger log = LoggerFactory.getLogger(WebSocketFrameHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof BinaryWebSocketFrame binaryFrame) {
            // 二进制帧：传递给后续处理器
            ByteBuf content = binaryFrame.content();
            content.retain();
            ctx.fireChannelRead(content);
        } else if (frame instanceof TextWebSocketFrame textFrame) {
            // 文本帧：也支持，转换为 ByteBuf
            ByteBuf content = textFrame.content();
            content.retain();
            ctx.fireChannelRead(content);
        } else if (frame instanceof PingWebSocketFrame) {
            // Ping 帧：回复 Pong
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
        } else if (frame instanceof PongWebSocketFrame) {
            // Pong 帧：忽略
            log.trace("Received pong from {}", ctx.channel().remoteAddress());
        } else if (frame instanceof CloseWebSocketFrame closeFrame) {
            // 关闭帧：关闭连接
            log.debug("WebSocket close frame received from {}, reason: {}",
                    ctx.channel().remoteAddress(), closeFrame.reasonText());
            ctx.close();
        } else {
            log.warn("Unsupported WebSocket frame type: {}", frame.getClass().getName());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("WebSocket frame handler error, channel: {}", ctx.channel().remoteAddress(), cause);
        ctx.close();
    }
}

