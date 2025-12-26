package org.markeb.actor.game;

import org.markeb.actor.ActorContext;
import org.markeb.actor.ActorRef;
import org.markeb.actor.ActorSystem;
import io.netty.channel.Channel;

import java.util.concurrent.CompletableFuture;

/**
 * 游戏 Actor 基类
 * <p>
 * 为游戏场景设计的 Actor 基类，封装了常用的游戏逻辑。
 * 可用于 Player、NPC、Room 等游戏实体。
 * </p>
 *
 * @param <S> 状态类型
 */
public abstract class GameActor<S> {

    private final String actorId;
    private final ActorRef actorRef;
    private Channel channel;

    protected GameActor(String actorId, ActorRef actorRef) {
        this.actorId = actorId;
        this.actorRef = actorRef;
    }

    /**
     * 获取 Actor ID
     */
    public String getActorId() {
        return actorId;
    }

    /**
     * 获取 Actor 引用
     */
    public ActorRef getActorRef() {
        return actorRef;
    }

    /**
     * 获取关联的 Channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * 设置关联的 Channel
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    /**
     * 发送消息给自己
     */
    public void tell(Object message) {
        actorRef.tell(message);
    }

    /**
     * 发送消息并等待响应
     */
    public <T> CompletableFuture<T> ask(Object message) {
        return actorRef.ask(message);
    }

    /**
     * 向客户端发送消息
     */
    public void sendToClient(Object message) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(message);
        }
    }

    /**
     * 检查是否在线
     */
    public boolean isOnline() {
        return channel != null && channel.isActive();
    }

    /**
     * 处理消息的抽象方法
     * <p>
     * 子类需要实现此方法来定义消息处理逻辑。
     * </p>
     *
     * @param context 上下文
     * @param state   当前状态
     * @param message 消息
     * @return 新状态
     * @throws Exception 处理异常
     */
    public abstract S onMessage(ActorContext context, S state, Object message) throws Exception;

    /**
     * Actor 启动时的回调
     */
    public void onStart() {
    }

    /**
     * Actor 停止时的回调
     */
    public void onStop() {
    }

}

