package org.markeb.game.actor;

import org.markeb.actor.ActorConfig;
import org.markeb.actor.ActorRef;
import org.markeb.actor.ActorSystem;
import org.markeb.actor.message.NetworkMessage;
import org.markeb.actor.message.SystemMessage;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 玩家 Actor 服务
 * <p>
 * 管理玩家 Actor 的生命周期，提供玩家相关的操作接口。
 * </p>
 */
@Service
public class PlayerActorService {

    private static final Logger log = LoggerFactory.getLogger(PlayerActorService.class);

    private final ActorSystem actorSystem;
    private final PlayerActorBehavior playerBehavior;
    private final Map<Long, Channel> playerChannels = new ConcurrentHashMap<>();

    public PlayerActorService(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
        this.playerBehavior = new PlayerActorBehavior();
    }

    /**
     * 玩家登录
     *
     * @param playerId 玩家 ID
     * @param channel  连接通道
     * @return Actor 引用
     */
    public ActorRef login(long playerId, Channel channel) {
        // 检查是否已在线
        Optional<ActorRef> existing = actorSystem.lookup(playerId);
        if (existing.isPresent()) {
            log.info("Player {} already online, kicking old connection", playerId);
            // 踢掉旧连接
            Channel oldChannel = playerChannels.get(playerId);
            if (oldChannel != null && oldChannel.isActive()) {
                oldChannel.close();
            }
            actorSystem.stop(playerId);
        }

        // 创建初始状态
        PlayerState initialState = new PlayerState(playerId);

        // 创建 Actor
        ActorConfig config = ActorConfig.builder()
                .mailboxCapacity(1000)  // 限制邮箱大小
                .continueOnException(true)  // 异常后继续处理
                .build();

        ActorRef actorRef = actorSystem.spawn(playerId, initialState, playerBehavior, config);

        // 保存 Channel 映射
        playerChannels.put(playerId, channel);

        // 发送启动消息
        actorRef.tell(new SystemMessage.Start());

        log.info("Player {} logged in, executor type: {}", playerId, actorSystem.getExecutorType());
        return actorRef;
    }

    /**
     * 玩家登出
     *
     * @param playerId 玩家 ID
     */
    public void logout(long playerId) {
        // 发送停止消息
        actorSystem.tell(playerId, new SystemMessage.Stop());

        // 延迟停止 Actor，让停止消息有机会被处理
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            actorSystem.stop(playerId);
            playerChannels.remove(playerId);
            log.info("Player {} logged out", playerId);
        });
    }

    /**
     * 处理网络消息
     * <p>
     * 将网络层收到的消息转发给对应的玩家 Actor。
     * </p>
     *
     * @param playerId 玩家 ID
     * @param message  消息
     * @param channel  通道
     * @return 是否成功发送
     */
    public boolean handleMessage(long playerId, Object message, Channel channel) {
        NetworkMessage<?> networkMessage = new NetworkMessage<>(message, channel);
        return actorSystem.tell(playerId, networkMessage);
    }

    /**
     * 向玩家发送消息
     *
     * @param playerId 玩家 ID
     * @param message  消息
     * @return 是否成功
     */
    public boolean tell(long playerId, Object message) {
        return actorSystem.tell(playerId, message);
    }

    /**
     * 向玩家发送消息并等待响应
     *
     * @param playerId 玩家 ID
     * @param message  消息
     * @param <T>      响应类型
     * @return CompletableFuture
     */
    public <T> CompletableFuture<T> ask(long playerId, Object message) {
        return actorSystem.ask(playerId, message);
    }

    /**
     * 检查玩家是否在线
     *
     * @param playerId 玩家 ID
     * @return 是否在线
     */
    public boolean isOnline(long playerId) {
        return actorSystem.lookup(playerId).isPresent();
    }

    /**
     * 获取玩家的 Channel
     *
     * @param playerId 玩家 ID
     * @return Channel
     */
    public Optional<Channel> getChannel(long playerId) {
        return Optional.ofNullable(playerChannels.get(playerId));
    }

    /**
     * 向玩家客户端发送消息
     *
     * @param playerId 玩家 ID
     * @param message  消息
     */
    public void sendToClient(long playerId, Object message) {
        Channel channel = playerChannels.get(playerId);
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(message);
        }
    }

    /**
     * 广播消息给所有在线玩家
     *
     * @param message 消息
     */
    public void broadcast(Object message) {
        playerChannels.forEach((playerId, channel) -> {
            if (channel.isActive()) {
                channel.writeAndFlush(message);
            }
        });
    }

    /**
     * 获取在线玩家数量
     *
     * @return 在线玩家数
     */
    public int getOnlineCount() {
        return actorSystem.actorCount();
    }

}

