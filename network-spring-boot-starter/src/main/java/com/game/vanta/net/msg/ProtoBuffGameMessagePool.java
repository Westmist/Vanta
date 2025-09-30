package com.game.vanta.net.msg;

import com.game.vanta.net.codec.ProtoBuffGameDecoder;
import com.game.vanta.net.codec.ProtoBuffGameEncoder;
import com.game.vanta.net.register.GameActorContext;
import com.game.vanta.net.register.IContextHandle;
import com.google.protobuf.Message;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.HashMap;
import java.util.Map;

public class ProtoBuffGameMessagePool implements IMessagePool<Message> {

    private final IGameParser<?> protoBuffParser;

    private static final Map<Integer, IContextHandle<? extends GameActorContext, Message>> handlerPool = new HashMap<>();

    public ProtoBuffGameMessagePool(IGameParser<?> protoBuffParser) {
        this.protoBuffParser = protoBuffParser;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IGameParser<Message> messageParser() {
        return (IGameParser<Message>) protoBuffParser;
    }

    @Override
    public MessageToByteEncoder<Message> encoder() {
        return new ProtoBuffGameEncoder(messageParser());
    }

    @Override
    public ByteToMessageDecoder decoder() {
        return new ProtoBuffGameDecoder(messageParser());
    }

    @Override
    public void register(int msgId, IContextHandle<? extends GameActorContext, Message> contextHandle) {
        if (handlerPool.containsKey(msgId)) {
            throw new IllegalArgumentException("Handler already registered for message ID: " + msgId);
        }
        handlerPool.put(msgId, contextHandle);
    }

    @Override
    @SuppressWarnings("unchecked")
    public IContextHandle<? extends GameActorContext, Message> getHandler(Message message) {
        IGameParser<Message> parser = messageParser();
        Class<Message> clazz = (Class<Message>) message.getClass();
        int messageId = parser.messageId(clazz);
        return handlerPool.get(messageId);
    }

}
