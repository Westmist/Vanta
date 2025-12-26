package org.markeb.actor.game;

import org.markeb.actor.ActorContext;
import org.markeb.actor.ActorRef;
import io.netty.channel.Channel;

/**
 * 玩家 Actor 基类
 * <p>
 * 为玩家实体设计的 Actor 基类，封装了玩家相关的通用逻辑。
 * </p>
 *
 * @param <S> 玩家状态类型
 */
public abstract class PlayerActor<S> extends GameActor<S> {

    private long playerId;
    private long accountId;
    private long loginTime;
    private long lastActiveTime;

    protected PlayerActor(long actorId, ActorRef actorRef) {
        super(actorId, actorRef);
    }

    /**
     * 获取玩家 ID
     */
    public long getPlayerId() {
        return playerId;
    }

    /**
     * 设置玩家 ID
     */
    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    /**
     * 获取账号 ID
     */
    public long getAccountId() {
        return accountId;
    }

    /**
     * 设置账号 ID
     */
    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    /**
     * 获取登录时间
     */
    public long getLoginTime() {
        return loginTime;
    }

    /**
     * 获取最后活跃时间
     */
    public long getLastActiveTime() {
        return lastActiveTime;
    }

    /**
     * 更新最后活跃时间
     */
    public void updateActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }

    /**
     * 获取在线时长（毫秒）
     */
    public long getOnlineDuration() {
        return System.currentTimeMillis() - loginTime;
    }

    /**
     * 处理玩家登录
     */
    public void onLogin(Channel channel) {
        setChannel(channel);
        this.loginTime = System.currentTimeMillis();
        this.lastActiveTime = this.loginTime;
    }

    /**
     * 处理玩家登出
     */
    public void onLogout() {
        setChannel(null);
    }

    /**
     * 踢下线
     *
     * @param reason 原因
     */
    public void kick(String reason) {
        Channel channel = getChannel();
        if (channel != null && channel.isActive()) {
            // 可以发送踢下线消息给客户端
            channel.close();
        }
        onLogout();
    }

    @Override
    public S onMessage(ActorContext context, S state, Object message) throws Exception {
        updateActiveTime();
        return handleMessage(context, state, message);
    }

    /**
     * 处理消息的抽象方法
     */
    protected abstract S handleMessage(ActorContext context, S state, Object message) throws Exception;

}

