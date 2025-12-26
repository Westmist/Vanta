package org.markeb.actor.message;

import io.netty.channel.Channel;

/**
 * 网络消息包装
 * <p>
 * 将网络层收到的消息包装成 Actor 消息，包含原始消息和 Channel 信息。
 * </p>
 *
 * @param <T> 消息类型
 */
public class NetworkMessage<T> implements ActorMessage {

    private final T payload;
    private final Channel channel;
    private final long timestamp;

    public NetworkMessage(T payload, Channel channel) {
        this.payload = payload;
        this.channel = channel;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 获取消息负载
     */
    public T getPayload() {
        return payload;
    }

    /**
     * 获取 Channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * 获取消息时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 向客户端发送响应
     */
    public void reply(Object response) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(response);
        }
    }

    @Override
    public String toString() {
        return "NetworkMessage{" +
                "payload=" + payload.getClass().getSimpleName() +
                ", channel=" + (channel != null ? channel.remoteAddress() : "null") +
                '}';
    }

}

