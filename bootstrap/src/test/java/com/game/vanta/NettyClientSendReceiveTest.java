package com.game.vanta;

import com.game.vanta.BootstrapApplication;
import com.game.vanta.net.NetworkAutoConfiguration;
import com.game.vanta.net.msg.IMessagePool;
import com.google.protobuf.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestPropertySource(properties = "network.enabled=false")
@SpringBootTest(
        classes = {
                NetworkAutoConfiguration.class,
                BootstrapApplication.class
        }
)
class NettyClientSendReceiveTest {

    @Autowired
    private IMessagePool<Message> messagePool;

    @Test
    void contextLoads() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        AtomicBoolean received = new AtomicBoolean(false);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline().addLast(messagePool.decoder());
                            ch.pipeline().addLast(messagePool.encoder());
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<Message>() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    var testMessage = com.game.vanta.proto.Test.ReqTestMessage.newBuilder()
                                            .setId(1)
                                            .setName("Test")
                                            .build();
                                    try {
                                        ctx.writeAndFlush(testMessage).sync();
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
                                    System.out.println("收到服务端响应: " + msg);
                                    received.set(true);
                                    ctx.close();
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    cause.printStackTrace();
                                    ctx.close();
                                }
                            });
                        }
                    });

            bootstrap.connect("localhost", 9201).sync().channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }

        assertTrue(received.get(), "未收到服务端响应");
    }


}
