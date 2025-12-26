package org.markeb.gateway.backend;

import org.markeb.net.gateway.GatewayPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 后端连接处理器
 * 处理游戏节点的响应
 */
public class BackendChannelHandler extends SimpleChannelInboundHandler<GatewayPacket> {

    private static final Logger log = LoggerFactory.getLogger(BackendChannelHandler.class);

    private final BackendChannelManager channelManager;
    private final String nodeAddress;

    public BackendChannelHandler(BackendChannelManager channelManager, String nodeAddress) {
        this.channelManager = channelManager;
        this.nodeAddress = nodeAddress;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GatewayPacket msg) {
        // 收到后端响应，转发给对应的前端会话
        channelManager.handleResponse(nodeAddress, msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Backend channel active: {}", nodeAddress);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Backend channel inactive: {}", nodeAddress);
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent idleEvent) {
            if (idleEvent.state() == IdleState.WRITER_IDLE) {
                // 发送心跳包保持连接
                log.debug("Sending heartbeat to backend: {}", nodeAddress);
                // 可以发送一个心跳包
                // ctx.writeAndFlush(heartbeatPacket);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Backend channel error: {}", nodeAddress, cause);
        ctx.close();
    }
}

