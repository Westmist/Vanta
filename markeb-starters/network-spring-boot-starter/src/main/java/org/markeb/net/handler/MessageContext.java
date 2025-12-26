package org.markeb.net.handler;

import io.netty.channel.Channel;
import lombok.Data;

/**
 * 消息上下文
 * 包含消息处理所需的上下文信息
 */
@Data
public class MessageContext {

    /**
     * 网络通道
     */
    private Channel channel;

    /**
     * 消息ID
     */
    private int messageId;

    /**
     * 序列号
     */
    private short seq;

    /**
     * 网关ID（仅游戏服协议有效）
     */
    private short gateId;

    /**
     * 角色ID（仅游戏服协议有效）
     */
    private long roleId;

    /**
     * 连接ID（仅游戏服协议有效）
     */
    private long conId;

    /**
     * 获取远程地址
     */
    public String getRemoteAddress() {
        return channel != null ? channel.remoteAddress().toString() : null;
    }

    /**
     * 发送消息
     */
    public void write(Object message) {
        if (channel != null && channel.isActive()) {
            channel.write(message);
        }
    }

    /**
     * 发送消息并刷新
     */
    public void writeAndFlush(Object message) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(message);
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (channel != null) {
            channel.close();
        }
    }
}

