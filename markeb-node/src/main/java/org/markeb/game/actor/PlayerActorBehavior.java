package org.markeb.game.actor;

import org.markeb.actor.ActorBehavior;
import org.markeb.actor.ActorContext;
import org.markeb.actor.message.NetworkMessage;
import org.markeb.actor.message.SystemMessage;
import org.markeb.proto.message.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 玩家 Actor 行为定义
 * <p>
 * 定义玩家 Actor 如何处理各种消息。
 * 所有消息处理都是串行的，无需担心并发问题。
 * </p>
 */
public class PlayerActorBehavior implements ActorBehavior<PlayerState> {

    private static final Logger log = LoggerFactory.getLogger(PlayerActorBehavior.class);

    @Override
    public PlayerState onMessage(ActorContext context, PlayerState state, Object message) throws Exception {
        log.debug("Player {} received message: {}", state.getPlayerId(), message.getClass().getSimpleName());

        // 处理系统消息
        if (message instanceof SystemMessage systemMessage) {
            return handleSystemMessage(context, state, systemMessage);
        }

        // 处理网络消息
        if (message instanceof NetworkMessage<?> networkMessage) {
            return handleNetworkMessage(context, state, networkMessage);
        }

        // 处理其他消息
        return handleOtherMessage(context, state, message);
    }

    private PlayerState handleSystemMessage(ActorContext context, PlayerState state, SystemMessage message) {
        switch (message) {
            case SystemMessage.Start start -> {
                log.info("Player {} actor started", state.getPlayerId());
                state.setLastLoginTime(System.currentTimeMillis());
            }
            case SystemMessage.Stop stop -> {
                log.info("Player {} actor stopping", state.getPlayerId());
                state.setLastLogoutTime(System.currentTimeMillis());
            }
            case SystemMessage.Tick tick -> {
                // 定时器心跳，可以做一些定时任务
                log.trace("Player {} tick at {}", state.getPlayerId(), tick.timestamp());
            }
            case SystemMessage.Timer timer -> {
                log.debug("Player {} timer {} triggered", state.getPlayerId(), timer.timerId());
            }
            default -> log.warn("Unknown system message: {}", message);
        }
        return state;
    }

    private PlayerState handleNetworkMessage(ActorContext context, PlayerState state, NetworkMessage<?> message) {
        Object payload = message.getPayload();

        // 处理测试消息
        if (payload instanceof Test.ReqTestMessage req) {
            log.info("Player {} handling ReqTestMessage", state.getPlayerId());

            // 构建响应
            Test.ResTestMessage response = Test.ResTestMessage.newBuilder()
                    .setResult(true)
                    .build();

            // 发送响应
            message.reply(response);
        }

        // 处理其他协议消息...

        return state;
    }

    private PlayerState handleOtherMessage(ActorContext context, PlayerState state, Object message) {
        // 处理自定义消息类型
        log.debug("Player {} received custom message: {}", state.getPlayerId(), message);
        return state;
    }

}

