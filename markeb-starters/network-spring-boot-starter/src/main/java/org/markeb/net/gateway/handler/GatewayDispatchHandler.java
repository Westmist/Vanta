package org.markeb.net.gateway.handler;

import org.markeb.net.gateway.GatewayAttributes;
import org.markeb.net.gateway.GatewayPacket;
import org.markeb.net.gateway.backend.BackendConnectionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 前端连接入站 -> 转发到对应区服后端。
 * <p>
 * 职责：
 * 1. 为每个前端连接分配唯一 sessionId
 * 2. 将客户端协议包（无 sessionId）转换为内部协议包（有 sessionId）
 * 3. 转发到对应区服后端
 */
public class GatewayDispatchHandler extends SimpleChannelInboundHandler<GatewayPacket> {

    private static final Logger log = LoggerFactory.getLogger(GatewayDispatchHandler.class);

    /**
     * 全局 sessionId 生成器（单网关内唯一）
     * 如果需要多网关全局唯一，可以加上网关ID前缀或使用其他策略
     */
    private static final AtomicInteger SESSION_ID_GENERATOR = new AtomicInteger(0);

    private final BackendConnectionManager connectionManager;

    public GatewayDispatchHandler(BackendConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 为新连接分配 sessionId
        int sessionId = SESSION_ID_GENERATOR.incrementAndGet();
        ctx.channel().attr(GatewayAttributes.SESSION_ID).set(sessionId);
        log.info("New connection {} assigned sessionId {}", ctx.channel().id(), sessionId);
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GatewayPacket msg) {
        Channel channel = ctx.channel();
        String zoneId = channel.attr(GatewayAttributes.ZONE_ID).get();
        Integer sessionId = channel.attr(GatewayAttributes.SESSION_ID).get();

        if (zoneId == null) {
            log.warn("ZoneId not bound for channel {}, drop msgId {}", channel.id(), msg.getMsgId());
            return;
        }
        if (sessionId == null) {
            log.error("SessionId not found for channel {}, this should not happen", channel.id());
            return;
        }

        // 将客户端协议包转换为内部协议包（填充 sessionId）
        GatewayPacket internalPacket = new GatewayPacket(sessionId, msg.getMsgId(), msg.getSeq(), msg.getBody());

        connectionManager.forward(zoneId, internalPacket, channel)
            .exceptionally(ex -> {
                log.error("Forward failed zone {} sessionId {} msgId {} seq {}",
                    zoneId, sessionId, msg.getMsgId(), msg.getSeq(), ex);
                return null;
            });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Integer sessionId = ctx.channel().attr(GatewayAttributes.SESSION_ID).get();
        log.info("Connection {} (sessionId {}) closed", ctx.channel().id(), sessionId);
        // 通知 connectionManager 清理该 sessionId 相关的 pending
        if (sessionId != null) {
            connectionManager.cleanupSession(sessionId);
        }
        super.channelInactive(ctx);
    }
}

