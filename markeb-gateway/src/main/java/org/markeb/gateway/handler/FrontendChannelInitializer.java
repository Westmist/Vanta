package org.markeb.gateway.handler;

import org.markeb.gateway.backend.BackendChannelManager;
import org.markeb.gateway.route.NodeRouter;
import org.markeb.gateway.session.SessionManager;
import org.markeb.net.gateway.codec.GatewayDecoder;
import org.markeb.net.gateway.codec.GatewayEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * 前端连接初始化器
 */
public class FrontendChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final SessionManager sessionManager;
    private final BackendChannelManager backendChannelManager;
    private final NodeRouter nodeRouter;
    private final int readIdleTimeout;

    public FrontendChannelInitializer(SessionManager sessionManager,
                                       BackendChannelManager backendChannelManager,
                                       NodeRouter nodeRouter,
                                       int readIdleTimeout) {
        this.sessionManager = sessionManager;
        this.backendChannelManager = backendChannelManager;
        this.nodeRouter = nodeRouter;
        this.readIdleTimeout = readIdleTimeout;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline()
                // 空闲检测
                .addLast(new IdleStateHandler(readIdleTimeout, 0, 0, TimeUnit.SECONDS))
                // 编解码器（前端协议，不带 sessionId）
                .addLast(new GatewayDecoder(true))
                .addLast(new GatewayEncoder(true))
                // 业务处理器
                .addLast(new FrontendHandler(sessionManager, backendChannelManager, nodeRouter));
    }
}

