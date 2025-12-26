package org.markeb.net.handler;

import org.markeb.net.protocol.GameServerPacket;
import org.markeb.net.protocol.GatewayPacket;
import org.markeb.net.protocol.Packet;
import org.markeb.net.serialization.MessageCodec;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息分发器
 * 将收到的 Packet 解码为消息对象并分发给对应的处理器
 */
public class MessageDispatcher {

    private static final Logger log = LoggerFactory.getLogger(MessageDispatcher.class);

    private final MessageCodec messageCodec;
    private final Map<Integer, MessageHandler<?>> handlers = new ConcurrentHashMap<>();

    public MessageDispatcher(MessageCodec messageCodec) {
        this.messageCodec = messageCodec;
    }

    /**
     * 注册消息处理器
     */
    public <T> void registerHandler(int messageId, MessageHandler<T> handler) {
        if (handlers.containsKey(messageId)) {
            throw new IllegalArgumentException("Handler already registered for message ID: " + messageId);
        }
        handlers.put(messageId, handler);
        log.info("Registered handler for message ID: {}", messageId);
    }

    /**
     * 分发消息
     */
    @SuppressWarnings("unchecked")
    public void dispatch(ChannelHandlerContext ctx, Packet packet) {
        int messageId = packet.getMessageId();
        byte[] body = packet.getBody();

        // 解码消息体
        Object message = messageCodec.decode(messageId, body);
        if (message == null) {
            log.warn("Failed to decode message with ID: {}", messageId);
            return;
        }

        // 查找处理器
        MessageHandler<Object> handler = (MessageHandler<Object>) handlers.get(messageId);
        if (handler == null) {
            log.warn("No handler found for message ID: {}", messageId);
            return;
        }

        // 构建上下文
        MessageContext context = buildContext(ctx, packet);

        // 执行处理
        try {
            Object response = handler.handle(context, message);
            if (response != null) {
                sendResponse(ctx, packet, response);
            }
        } catch (Exception e) {
            log.error("Error handling message ID: {}", messageId, e);
        }
    }

    private MessageContext buildContext(ChannelHandlerContext ctx, Packet packet) {
        MessageContext context = new MessageContext();
        context.setChannel(ctx.channel());
        context.setMessageId(packet.getMessageId());
        context.setSeq(packet.getSeq());

        if (packet instanceof GameServerPacket gsp) {
            context.setGateId(gsp.getGateId());
            context.setRoleId(gsp.getRoleId());
            context.setConId(gsp.getConId());
        }

        return context;
    }

    private void sendResponse(ChannelHandlerContext ctx, Packet request, Object response) {
        byte[] responseBody = messageCodec.encode(response);
        Integer responseId = messageCodec.getCodecType() == org.markeb.net.serialization.CodecType.PROTOBUF
                ? getMessageId(response.getClass())
                : request.getMessageId();

        Packet responsePacket;
        if (request instanceof GameServerPacket gsp) {
            responsePacket = new GameServerPacket(
                    responseId != null ? responseId : request.getMessageId(),
                    request.getSeq(),
                    gsp.getGateId(),
                    gsp.getRoleId(),
                    gsp.getConId(),
                    responseBody
            );
        } else {
            responsePacket = new GatewayPacket(
                    responseId != null ? responseId : request.getMessageId(),
                    request.getSeq(),
                    responseBody
            );
        }

        ctx.writeAndFlush(responsePacket);
    }

    private Integer getMessageId(Class<?> clazz) {
        // 这里需要从 MessageRegistry 获取，暂时返回 null
        return null;
    }

    public MessageCodec getMessageCodec() {
        return messageCodec;
    }
}

