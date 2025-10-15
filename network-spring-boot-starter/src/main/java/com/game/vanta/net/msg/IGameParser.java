package com.game.vanta.net.msg;

public interface IGameParser<M> {

    int messageId(Class<M> message);

    Class<M> messageClazz();

    M parseFrom(int id, byte[] bodyBytes);

    void register(Class<M> message);
}
