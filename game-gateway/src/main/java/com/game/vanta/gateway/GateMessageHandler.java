package com.game.vanta.gateway;


import com.game.vanta.net.msg.IMessagePool;
import com.google.protobuf.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GateMessageHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger log = LoggerFactory.getLogger(GateMessageHandler.class);

    private final IMessagePool<Message> messagePool;

    public GateMessageHandler(IMessagePool<Message> messagePool) {
        this.messagePool = messagePool;
    }

    /**
     * 接收消息
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message msg) {
        log.info("Received message: {} from {}",
            msg.getClass().getSimpleName(), ctx.channel().remoteAddress());
    }

    /**
     * 建立新连接
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("Connected: {}", ctx.channel().remoteAddress());
    }

    /**
     * 断开连接
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        log.info("Disconnected: {}", channel.remoteAddress());
    }

    /**
     * 有异常发生
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Connection exception: {}", ctx.channel().remoteAddress(), cause);
    }

    /**
     * 通道可写状态改变
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
        log.info("Channel writability changed: {} isWritable: {}",
            ctx.channel().remoteAddress(), ctx.channel().isWritable());
    }

    /**
     * 心跳检测
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        log.info("User event triggered: {} event: {}",
            ctx.channel().remoteAddress(), evt.getClass().getSimpleName());
        ctx.channel().close();
    }

}
