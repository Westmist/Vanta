package org.markeb.game.netty;

import org.markeb.actor.ActorRef;
import org.markeb.actor.message.NetworkMessage;
import com.google.protobuf.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.markeb.game.actor.PlayerActorService;

import static org.markeb.game.netty.ChannelAttributeKey.PLAYER_ID_KEY;

/**
 * 基于 Actor 模型的服务端处理器
 * <p>
 * 将网络消息转发给对应的玩家 Actor 处理，实现消息的串行化处理。
 * </p>
 */
public class ActorServerHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger log = LoggerFactory.getLogger(ActorServerHandler.class);

    private final PlayerActorService playerActorService;

    public ActorServerHandler(PlayerActorService playerActorService) {
        this.playerActorService = playerActorService;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message msg) {
        Channel channel = ctx.channel();
        String playerId = channel.attr(PLAYER_ID_KEY).get();

        if (playerId == null) {
            log.warn("Received message from unauthenticated channel: {}", channel.remoteAddress());
            // 这里可以处理登录消息
            handleLoginMessage(ctx, msg);
            return;
        }

        // 将消息转发给玩家 Actor
        boolean sent = playerActorService.handleMessage(playerId, msg, channel);
        if (!sent) {
            log.warn("Failed to send message to player {}, actor may not exist", playerId);
        }

        log.debug("Forwarded message {} to player {}", msg.getClass().getSimpleName(), playerId);
    }

    /**
     * 处理登录消息
     */
    private void handleLoginMessage(ChannelHandlerContext ctx, Message msg) {
        // TODO: 实现登录逻辑，验证后创建玩家 Actor
        // 示例：假设第一条消息包含玩家 ID
        String playerId = extractPlayerId(msg);
        if (playerId != null) {
            Channel channel = ctx.channel();

            // 创建玩家 Actor
            ActorRef actorRef = playerActorService.login(playerId, channel);

            // 保存玩家 ID 到 Channel
            channel.attr(PLAYER_ID_KEY).set(playerId);

            log.info("Player {} authenticated, actor created", playerId);

            // 转发登录消息给 Actor
            playerActorService.handleMessage(playerId, msg, channel);
        }
    }

    /**
     * 从消息中提取玩家 ID
     */
    private String extractPlayerId(Message msg) {
        // TODO: 根据实际的登录协议实现
        // 这里返回一个临时 ID 用于测试
        return "player_" + System.currentTimeMillis();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        log.info("New connection: {}", channel.remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        String playerId = channel.attr(PLAYER_ID_KEY).get();

        if (playerId != null) {
            // 通知玩家 Actor 下线
            playerActorService.logout(playerId);
            log.info("Player {} disconnected", playerId);
        } else {
            log.info("Unauthenticated connection closed: {}", channel.remoteAddress());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();
        String playerId = channel.attr(PLAYER_ID_KEY).get();
        log.error("Connection exception for player {}: {}", playerId, channel.remoteAddress(), cause);
        ctx.close();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        if (!channel.isWritable()) {
            String playerId = channel.attr(PLAYER_ID_KEY).get();
            log.warn("Channel not writable for player {}", playerId);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        // 处理心跳超时等事件
        log.debug("User event: {} from {}", evt.getClass().getSimpleName(), ctx.channel().remoteAddress());
    }

}

