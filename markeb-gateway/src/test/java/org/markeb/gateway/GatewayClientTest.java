package org.markeb.gateway;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.junit.jupiter.api.Test;
import org.markeb.proto.message.Login.ReqLoginMessage;
import org.markeb.proto.message.Login.ResLoginMessage;
import org.markeb.proto.message.Test.ReqTestMessage;
import org.markeb.proto.message.Test.ResTestMessage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 网关客户端测试：测试连接网关并发送/接收 protobuf 消息。
 * <p>
 * 客户端 -> 网关协议头： length(4) + msgId(4) + seq(4) + body(n)
 * <p>
 * 使用前确保网关服务已启动在 7000 端口。
 */
class GatewayClientTest {

    private static final String GATEWAY_HOST = "127.0.0.1";
    private static final int GATEWAY_PORT = 7000;

    /**
     * 测试连接网关并发送登录请求
     */
    @Test
    void testConnectAndSendLoginMessage() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        AtomicBoolean connected = new AtomicBoolean(false);
        AtomicBoolean messageSent = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                            // 解码器：按长度字段分帧
                            .addLast(new LengthFieldBasedFrameDecoder(
                                1024 * 1024, // maxFrameLength
                                0,           // lengthFieldOffset
                                4,           // lengthFieldLength
                                0,           // lengthAdjustment
                                4))          // initialBytesToStrip
                            // 编码器
                            .addLast(new ClientEncoder())
                            // 业务处理器
                            .addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    connected.set(true);
                                    System.out.println("[客户端] 已连接到网关: " + ctx.channel().remoteAddress());

                                    // 发送登录请求
                                    ReqLoginMessage loginMsg = ReqLoginMessage.newBuilder()
                                        .setOpenId("test_user_001")
                                        .setToken("test_token_abc123")
                                        .build();

                                    ClientPacket packet = new ClientPacket(11000, 1, loginMsg.toByteArray());
                                    ctx.writeAndFlush(packet);
                                    messageSent.set(true);
                                    System.out.println("[客户端] 已发送登录请求: openId=test_user_001");
                                }

                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                    int msgId = msg.readInt();
                                    int seq = msg.readInt();
                                    byte[] body = new byte[msg.readableBytes()];
                                    msg.readBytes(body);

                                    System.out.println("[客户端] 收到响应: msgId=" + msgId + ", seq=" + seq + ", bodyLen=" + body.length);

                                    // 尝试解析响应
                                    try {
                                        if (msgId == 11001) {
                                            ResLoginMessage response = ResLoginMessage.parseFrom(body);
                                            System.out.println("[客户端] 登录响应: openId=" + response.getOpenId() + ", success=" + response.getSuccess());
                                        }
                                    } catch (Exception e) {
                                        System.out.println("[客户端] 解析响应失败: " + e.getMessage());
                                    }

                                    latch.countDown();
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    System.err.println("[客户端] 异常: " + cause.getMessage());
                                    cause.printStackTrace();
                                    ctx.close();
                                    latch.countDown();
                                }
                            });
                    }
                });

            // 连接网关
            System.out.println("[客户端] 正在连接网关: " + GATEWAY_HOST + ":" + GATEWAY_PORT);
            ChannelFuture cf = bootstrap.connect(GATEWAY_HOST, GATEWAY_PORT).sync();

            // 等待响应或超时
            boolean received = latch.await(5, TimeUnit.SECONDS);
            System.out.println("[客户端] 等待响应结果: " + (received ? "收到响应" : "超时"));

            // 关闭连接
            cf.channel().close();
            cf.channel().closeFuture().sync();

        } finally {
            group.shutdownGracefully().sync();
        }

        assertTrue(connected.get(), "未成功连接到网关");
        assertTrue(messageSent.get(), "消息未发送");
    }

    /**
     * 测试连接网关并发送测试消息
     */
    @Test
    void testConnectAndSendTestMessage() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        AtomicBoolean connected = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                            .addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4))
                            .addLast(new ClientEncoder())
                            .addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    connected.set(true);
                                    System.out.println("[客户端] 已连接到网关");

                                    // 发送测试请求
                                    ReqTestMessage testMsg = ReqTestMessage.newBuilder()
                                        .setId(12345)
                                        .setName("测试消息")
                                        .build();

                                    ClientPacket packet = new ClientPacket(10001, 1, testMsg.toByteArray());
                                    ctx.writeAndFlush(packet);
                                    System.out.println("[客户端] 已发送测试请求: id=12345, name=测试消息");
                                }

                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                    int msgId = msg.readInt();
                                    int seq = msg.readInt();
                                    byte[] body = new byte[msg.readableBytes()];
                                    msg.readBytes(body);

                                    System.out.println("[客户端] 收到响应: msgId=" + msgId + ", seq=" + seq);

                                    try {
                                        if (msgId == 10002) {
                                            ResTestMessage response = ResTestMessage.parseFrom(body);
                                            System.out.println("[客户端] 测试响应: result=" + response.getResult());
                                        }
                                    } catch (Exception e) {
                                        System.out.println("[客户端] 解析响应失败: " + e.getMessage());
                                    }

                                    latch.countDown();
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    System.err.println("[客户端] 异常: " + cause.getMessage());
                                    ctx.close();
                                    latch.countDown();
                                }
                            });
                    }
                });

            ChannelFuture cf = bootstrap.connect(GATEWAY_HOST, GATEWAY_PORT).sync();
            latch.await(5, TimeUnit.SECONDS);
            cf.channel().close();
            cf.channel().closeFuture().sync();

        } finally {
            group.shutdownGracefully().sync();
        }

        assertTrue(connected.get(), "未成功连接到网关");
    }

    /**
     * 测试连接网关并发送多条消息
     */
    @Test
    void testSendMultipleMessages() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        AtomicBoolean connected = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(3); // 等待3条响应

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                            .addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4))
                            .addLast(new ClientEncoder())
                            .addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) {
                                    connected.set(true);
                                    System.out.println("[客户端] 已连接到网关，准备发送多条消息");

                                    // 发送多条消息
                                    for (int i = 1; i <= 3; i++) {
                                        ReqTestMessage testMsg = ReqTestMessage.newBuilder()
                                            .setId(i)
                                            .setName("消息" + i)
                                            .build();

                                        ClientPacket packet = new ClientPacket(10001, i, testMsg.toByteArray());
                                        ctx.writeAndFlush(packet);
                                        System.out.println("[客户端] 发送消息 seq=" + i);
                                    }
                                }

                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
                                    int msgId = msg.readInt();
                                    int seq = msg.readInt();
                                    System.out.println("[客户端] 收到响应: msgId=" + msgId + ", seq=" + seq);
                                    latch.countDown();
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    System.err.println("[客户端] 异常: " + cause.getMessage());
                                    ctx.close();
                                }
                            });
                    }
                });

            ChannelFuture cf = bootstrap.connect(GATEWAY_HOST, GATEWAY_PORT).sync();
            boolean allReceived = latch.await(10, TimeUnit.SECONDS);
            System.out.println("[客户端] 收到所有响应: " + allReceived);
            cf.channel().close();
            cf.channel().closeFuture().sync();

        } finally {
            group.shutdownGracefully().sync();
        }

        assertTrue(connected.get(), "未成功连接到网关");
    }

    /**
     * 客户端数据包
     */
    private static class ClientPacket {
        final int msgId;
        final int seq;
        final byte[] body;

        ClientPacket(int msgId, int seq, byte[] body) {
            this.msgId = msgId;
            this.seq = seq;
            this.body = body;
        }
    }

    /**
     * 客户端编码器
     * 协议格式：length(4) + msgId(4) + seq(4) + body(n)
     */
    private static class ClientEncoder extends MessageToByteEncoder<ClientPacket> {
        @Override
        protected void encode(ChannelHandlerContext ctx, ClientPacket packet, ByteBuf out) {
            int bodyLen = packet.body == null ? 0 : packet.body.length;
            out.writeInt(8 + bodyLen);  // length = msgId(4) + seq(4) + body
            out.writeInt(packet.msgId);
            out.writeInt(packet.seq);
            if (bodyLen > 0) {
                out.writeBytes(packet.body);
            }
        }
    }
}

