package org.markeb.gateway;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 网关测试客户端：测试连接网关并发送协议包（不依赖 proto-message）。
 * 协议格式：length(4) + msgId(4) + seq(4) + body(n)
 */
class NettyClientSendReceiveTest {

    @Test
    void testConnectGateway() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        AtomicBoolean connected = new AtomicBoolean(false);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        // 网关协议：length(4) + msgId(4) + seq(4) + body(n)
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) {
                                connected.set(true);
                                // 发送一个测试包：msgId=1, seq=1, body="test"
                                ByteBuf buf = ctx.alloc().buffer();
                                byte[] body = "test".getBytes();
                                buf.writeInt(8 + body.length); // length
                                buf.writeInt(1); // msgId
                                buf.writeInt(1); // seq
                                buf.writeBytes(body);
                                ctx.writeAndFlush(buf);
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                if (msg instanceof ByteBuf) {
                                    ByteBuf buf = (ByteBuf) msg;
                                    System.out.println("收到网关响应，长度: " + buf.readableBytes());
                                    buf.release();
                                }
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                cause.printStackTrace();
                                ctx.close();
                            }
                        });
                    }
                });

            // 连接网关端口（application.yaml 里配置的 9300）
            ChannelFuture cf = bootstrap.connect("127.0.0.1", 9300).sync();
            Thread.sleep(1000); // 等待一下
            cf.channel().close();
            cf.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }

        assertTrue(connected.get(), "未成功连接到网关");
    }
}
