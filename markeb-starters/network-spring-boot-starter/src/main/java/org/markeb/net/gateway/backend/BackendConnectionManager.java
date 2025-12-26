package org.markeb.net.gateway.backend;

import org.markeb.net.gateway.GatewayPacket;
import org.markeb.net.gateway.config.GatewayBackendProperties;
import org.markeb.net.gateway.codec.GatewayDecoder;
import org.markeb.net.gateway.codec.GatewayEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 维护网关到各区服后端的长连接。
 * <p>
 * 使用 (sessionId:seq) 作为 pending 映射的 key，确保不同连接的请求不会冲突。
 */
public class BackendConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(BackendConnectionManager.class);

    private final GatewayBackendProperties backendProperties;
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    /**
     * zoneId -> Channel（到后端游戏服的连接）
     */
    private final Map<String, Channel> backendChannels = new ConcurrentHashMap<>();

    /**
     * pendingKey (sessionId:seq) -> 前端 Channel
     * 游戏服回包时，用 sessionId:seq 找到对应的前端连接
     */
    private final ConcurrentHashMap<String, Channel> pendingRequests = new ConcurrentHashMap<>();

    public BackendConnectionManager(GatewayBackendProperties backendProperties) {
        this.backendProperties = backendProperties;
    }

    public void shutdown() {
        backendChannels.values().forEach(ch -> {
            try {
                ch.close().sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        workerGroup.shutdownGracefully();
    }

    /**
     * 转发请求到后端游戏服
     *
     * @param zoneId       区服ID
     * @param packet       内部协议包（已包含 sessionId）
     * @param frontChannel 前端玩家连接
     */
    public CompletableFuture<Void> forward(String zoneId, GatewayPacket packet, Channel frontChannel) {
        Objects.requireNonNull(zoneId, "zoneId");
        Channel backend = backendChannels.computeIfAbsent(zoneId, this::connect);
        if (backend == null || !backend.isActive()) {
            return CompletableFuture.failedFuture(new IllegalStateException("backend not available for zone " + zoneId));
        }

        // 用 sessionId:seq 作为 key，确保不同连接的请求不会冲突
        String pendingKey = packet.getPendingKey();
        pendingRequests.put(pendingKey, frontChannel);

        CompletableFuture<Void> future = new CompletableFuture<>();
        backend.writeAndFlush(packet).addListener(f -> {
            if (f.isSuccess()) {
                future.complete(null);
            } else {
                future.completeExceptionally(f.cause());
                pendingRequests.remove(pendingKey);
            }
        });
        return future;
    }

    private Channel connect(String zoneId) {
        String target = backendProperties.getZones().get(zoneId);
        if (!StringUtils.hasText(target)) {
            log.error("zone {} target not configured", zoneId);
            return null;
        }
        String[] hp = target.split(":");
        if (hp.length != 2) {
            log.error("invalid backend address for zone {}: {}", zoneId, target);
            return null;
        }
        String host = hp[0];
        int port = Integer.parseInt(hp[1]);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    // 后端连接使用内部协议（有 sessionId）
                    ch.pipeline().addLast(new GatewayDecoder(false));
                    ch.pipeline().addLast(new GatewayEncoder(false));
                    ch.pipeline().addLast(new GatewayBackendHandler(BackendConnectionManager.this, zoneId));
                }
            });
        try {
            ChannelFuture cf = bootstrap.connect(new InetSocketAddress(host, port)).sync();
            log.info("Connected backend zone {} -> {}:{}", zoneId, host, port);
            return cf.channel();
        } catch (Exception e) {
            log.error("Connect backend failed zone {} target {}", zoneId, target, e);
            return null;
        }
    }

    /**
     * 处理后端游戏服的响应，路由回对应的前端连接
     */
    void handleResponse(String zoneId, GatewayPacket packet) {
        String pendingKey = packet.getPendingKey();
        Channel front = pendingRequests.remove(pendingKey);
        if (front == null || !front.isActive()) {
            log.warn("Front channel missing/closed for pendingKey {} zone {}", pendingKey, zoneId);
            return;
        }
        // 回给客户端时，转换为客户端协议（不含 sessionId）
        // 注意：这里直接写 packet，由 GatewayEncoder(forFrontend=true) 处理
        front.writeAndFlush(packet);
    }

    /**
     * 清理指定 sessionId 相关的所有 pending 请求（连接断开时调用）
     */
    public void cleanupSession(int sessionId) {
        String prefix = sessionId + ":";
        Iterator<Map.Entry<String, Channel>> it = pendingRequests.entrySet().iterator();
        int count = 0;
        while (it.hasNext()) {
            Map.Entry<String, Channel> entry = it.next();
            if (entry.getKey().startsWith(prefix)) {
                it.remove();
                count++;
            }
        }
        if (count > 0) {
            log.info("Cleaned up {} pending requests for sessionId {}", count, sessionId);
        }
    }
}

