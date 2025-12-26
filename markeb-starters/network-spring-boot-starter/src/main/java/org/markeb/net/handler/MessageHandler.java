package org.markeb.net.handler;

/**
 * 消息处理器接口
 *
 * @param <T> 消息类型
 */
@FunctionalInterface
public interface MessageHandler<T> {

    /**
     * 处理消息
     *
     * @param context 消息上下文
     * @param message 消息对象
     * @return 响应消息，如果为 null 则不发送响应
     */
    Object handle(MessageContext context, T message) throws Exception;
}

