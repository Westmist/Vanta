package org.markeb.actor.game;

import org.markeb.actor.ActorBehavior;
import org.markeb.actor.ActorConfig;
import org.markeb.actor.ActorRef;
import org.markeb.actor.ActorSystem;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 玩家 Actor 管理器
 * <p>
 * 管理玩家 Actor 的生命周期，提供玩家查找和消息发送功能。
 * </p>
 *
 * @param <P> 玩家 Actor 类型
 * @param <S> 玩家状态类型
 */
public class PlayerActorManager<P extends PlayerActor<S>, S> {

    private static final Logger log = LoggerFactory.getLogger(PlayerActorManager.class);

    private final ActorSystem actorSystem;
    private final Map<Long, P> playerActors = new ConcurrentHashMap<>();
    private final Function<Long, P> playerFactory;
    private final Function<P, S> initialStateFactory;
    private final ActorConfig actorConfig;

    /**
     * 创建玩家 Actor 管理器
     *
     * @param actorSystem         Actor 系统
     * @param playerFactory       玩家工厂（playerId -> PlayerActor）
     * @param initialStateFactory 初始状态工厂（PlayerActor -> State）
     */
    public PlayerActorManager(ActorSystem actorSystem,
                               Function<Long, P> playerFactory,
                               Function<P, S> initialStateFactory) {
        this(actorSystem, playerFactory, initialStateFactory, ActorConfig.defaults());
    }

    /**
     * 创建玩家 Actor 管理器
     *
     * @param actorSystem         Actor 系统
     * @param playerFactory       玩家工厂
     * @param initialStateFactory 初始状态工厂
     * @param actorConfig         Actor 配置
     */
    public PlayerActorManager(ActorSystem actorSystem,
                               Function<Long, P> playerFactory,
                               Function<P, S> initialStateFactory,
                               ActorConfig actorConfig) {
        this.actorSystem = actorSystem;
        this.playerFactory = playerFactory;
        this.initialStateFactory = initialStateFactory;
        this.actorConfig = actorConfig;
    }

    /**
     * 玩家登录
     *
     * @param playerId 玩家 ID
     * @param channel  连接通道
     * @return 玩家 Actor
     */
    public P login(long playerId, Channel channel) {
        P existingPlayer = playerActors.get(playerId);
        if (existingPlayer != null) {
            // 踢掉旧连接
            log.info("Player {} already online, kicking old connection", playerId);
            existingPlayer.kick("Duplicate login");
            playerActors.remove(playerId);
            actorSystem.stop(playerId);
        }

        // 创建新的玩家 Actor
        P player = playerFactory.apply(playerId);
        S initialState = initialStateFactory.apply(player);

        ActorBehavior<S> behavior = (context, state, message) ->
                player.onMessage(context, state, message);

        ActorRef actorRef = actorSystem.spawn(playerId, initialState, behavior, actorConfig);

        // 设置 ActorRef（需要通过反射或其他方式，这里假设有 setter）
        // player.setActorRef(actorRef);

        player.onLogin(channel);
        playerActors.put(playerId, player);

        log.info("Player {} logged in", playerId);
        return player;
    }

    /**
     * 玩家登出
     *
     * @param playerId 玩家 ID
     */
    public void logout(long playerId) {
        P player = playerActors.remove(playerId);
        if (player != null) {
            player.onLogout();
            player.onStop();
            actorSystem.stop(playerId);
            log.info("Player {} logged out", playerId);
        }
    }

    /**
     * 获取在线玩家
     *
     * @param playerId 玩家 ID
     * @return 玩家 Actor
     */
    public Optional<P> getPlayer(long playerId) {
        return Optional.ofNullable(playerActors.get(playerId));
    }

    /**
     * 检查玩家是否在线
     *
     * @param playerId 玩家 ID
     * @return 是否在线
     */
    public boolean isOnline(long playerId) {
        return playerActors.containsKey(playerId);
    }

    /**
     * 向玩家发送消息
     *
     * @param playerId 玩家 ID
     * @param message  消息
     * @return 是否发送成功
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
     * 广播消息给所有在线玩家
     *
     * @param message 消息
     */
    public void broadcast(Object message) {
        playerActors.keySet().forEach(playerId -> tell(playerId, message));
    }

    /**
     * 向玩家客户端发送消息
     *
     * @param playerId 玩家 ID
     * @param message  消息
     */
    public void sendToClient(long playerId, Object message) {
        P player = playerActors.get(playerId);
        if (player != null) {
            player.sendToClient(message);
        }
    }

    /**
     * 获取在线玩家数量
     *
     * @return 在线玩家数
     */
    public int getOnlineCount() {
        return playerActors.size();
    }

    /**
     * 获取所有在线玩家 ID
     *
     * @return 玩家 ID 集合
     */
    public Iterable<Long> getOnlinePlayerIds() {
        return playerActors.keySet();
    }

}

