package org.markeb.net.msg;

import org.markeb.net.register.GameActorContext;
import org.markeb.net.register.IContextHandle;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

public interface IMessagePool<M> {

    IGameParser<M> messageParser();

    MessageToByteEncoder<M> encoder();

    ByteToMessageDecoder decoder();

    void register(int msgId, IContextHandle<? extends GameActorContext, M> contextHandle);

    IContextHandle<? extends GameActorContext, M> getHandler(M message);

}
