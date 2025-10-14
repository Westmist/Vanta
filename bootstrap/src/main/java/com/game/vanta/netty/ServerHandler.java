package com.game.vanta.netty;

import com.game.vanta.actor.Player;
import com.game.vanta.manager.PlayerManager;
import com.game.vanta.net.msg.IMessagePool;
import com.game.vanta.net.register.IContextHandle;
import com.google.protobuf.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {

  private static final Logger log = LoggerFactory.getLogger(ServerHandler.class);

  private final IMessagePool<Message> messagePool;

  public ServerHandler(IMessagePool<Message> messagePool) {
    this.messagePool = messagePool;
  }

  /** 接收消息 */
  @Override
  public void channelRead0(ChannelHandlerContext ctx, Message msg) {
    Channel channel = ctx.channel();
    IContextHandle<Player, Message> handler =
        (IContextHandle<Player, Message>) messagePool.getHandler(msg);
    if (handler != null) {
      Player player = PlayerManager.getInstance().getPlayer(ctx.channel());
      try {
        Message rep = handler.invoke(player, msg);
        if (rep != null) {
          log.info(
              "Sending response: {} to {}",
              rep.getClass().getSimpleName(),
              channel.remoteAddress());
          ctx.writeAndFlush(rep);
        }
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    } else {
      log.warn("Message handler not found for message: {}", msg.getClass().getSimpleName());
    }
    log.info(
        "Received message: {} from {}", msg.getClass().getSimpleName(), channel.remoteAddress());
  }

  /** 建立新连接 */
  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    PlayerManager.getInstance().addPlayer(ctx.channel(), new Player(ctx.channel()));
    log.info("New connection: {}", ctx.channel().remoteAddress());
  }

  /** 断开连接 */
  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    log.info("Disconnected: {}", ctx.channel().remoteAddress());
    PlayerManager.getInstance().removePlayer(ctx.channel());
  }

  /** 有异常发生 */
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    log.error("Connection exception: {}", ctx.channel().remoteAddress(), cause);
  }

  /** 通道可写状态改变 */
  @Override
  public void channelWritabilityChanged(ChannelHandlerContext ctx) {
    log.info(
        "Channel writability changed: {} isWritable: {}",
        ctx.channel().remoteAddress(),
        ctx.channel().isWritable());
  }

  /** 心跳检测 */
  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
    log.info(
        "User event triggered: {} event: {}",
        ctx.channel().remoteAddress(),
        evt.getClass().getSimpleName());
  }
}
