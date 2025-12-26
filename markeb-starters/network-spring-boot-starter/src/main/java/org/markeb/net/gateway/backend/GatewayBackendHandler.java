package org.markeb.net.gateway.backend;

import org.markeb.net.gateway.GatewayPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理从游戏服回来的包，转发给前端客户端。
 */
public class GatewayBackendHandler extends SimpleChannelInboundHandler<GatewayPacket> {

    private static final Logger log = LoggerFactory.getLogger(GatewayBackendHandler.class);

    private final BackendConnectionManager connectionManager;
    private final String zoneId;

    public GatewayBackendHandler(BackendConnectionManager connectionManager, String zoneId) {
        this.connectionManager = connectionManager;
        this.zoneId = zoneId;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GatewayPacket msg) {
        connectionManager.handleResponse(zoneId, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Backend channel error for zone {}", zoneId, cause);
        ctx.close();
    }
}

