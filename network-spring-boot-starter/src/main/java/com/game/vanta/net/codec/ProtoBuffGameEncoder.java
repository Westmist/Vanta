package com.game.vanta.net.codec;

import com.game.vanta.net.msg.IGameParser;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 编码 消息结构 +----------+ | 总长度 | +----------+ | 消息ID | +----------+ | 主体数据 | +----------+ 总长度 =
 * 4(长度标示) + 4(消息ID) + 主体数据
 */
public class ProtoBuffGameEncoder extends MessageToByteEncoder<Message> {

  private final IGameParser<Message> parser;

  public ProtoBuffGameEncoder(IGameParser<Message> parser) {
    this.parser = parser;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) {
    Class<? extends Message> aClass = msg.getClass();
    int msgId = parser.messageId((Class<Message>) aClass);
    byte[] bytes = msg.toByteArray();
    out.writeInt(8 + bytes.length);
    out.writeInt(msgId);
    out.writeBytes(bytes);
  }
}
