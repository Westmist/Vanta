package org.markeb.net.transport.websocket;

import org.markeb.net.handler.MessageDispatcher;
import org.markeb.net.handler.PacketHandler;
import org.markeb.net.protocol.ProtocolType;
import org.markeb.net.protocol.codec.PacketDecoder;
import org.markeb.net.protocol.codec.PacketEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * WebSocket 服务器 Channel 初始化器
 */
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslContext;
    private final String websocketPath;
    private final int maxFrameSize;
    private final boolean enableCompression;
    private final long readerIdleTime;
    private final long writerIdleTime;
    private final long allIdleTime;
    private final ProtocolType protocolType;
    private final int maxFrameLength;
    private final MessageDispatcher messageDispatcher;

    public WebSocketServerInitializer(
            SslContext sslContext,
            String websocketPath,
            int maxFrameSize,
            boolean enableCompression,
            long readerIdleTime,
            long writerIdleTime,
            long allIdleTime,
            ProtocolType protocolType,
            int maxFrameLength,
            MessageDispatcher messageDispatcher) {
        this.sslContext = sslContext;
        this.websocketPath = websocketPath;
        this.maxFrameSize = maxFrameSize;
        this.enableCompression = enableCompression;
        this.readerIdleTime = readerIdleTime;
        this.writerIdleTime = writerIdleTime;
        this.allIdleTime = allIdleTime;
        this.protocolType = protocolType;
        this.maxFrameLength = maxFrameLength;
        this.messageDispatcher = messageDispatcher;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // SSL 支持（可选）
        if (sslContext != null) {
            pipeline.addLast("ssl", sslContext.newHandler(ch.alloc()));
        }

        // 空闲检测
        pipeline.addLast("idleStateHandler",
                new IdleStateHandler(readerIdleTime, writerIdleTime, allIdleTime, TimeUnit.SECONDS));

        // HTTP 编解码
        pipeline.addLast("httpServerCodec", new HttpServerCodec());
        pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("chunkedWriteHandler", new ChunkedWriteHandler());

        // WebSocket 压缩（可选）- 使用 permessage-deflate 扩展
        if (enableCompression) {
            @SuppressWarnings("deprecation")
            WebSocketServerCompressionHandler compressionHandler = new WebSocketServerCompressionHandler();
            pipeline.addLast("webSocketCompression", compressionHandler);
        }

        // WebSocket 协议处理
        pipeline.addLast("webSocketServerProtocolHandler",
                new WebSocketServerProtocolHandler(websocketPath, null, true, maxFrameSize));

        // WebSocket 帧处理 -> ByteBuf
        pipeline.addLast("webSocketFrameHandler", new WebSocketFrameHandler());

        // ByteBuf -> WebSocket 二进制帧
        pipeline.addLast("webSocketPacketEncoder", new WebSocketPacketEncoder());

        // 协议编解码（复用现有的 PacketDecoder/PacketEncoder）
        pipeline.addLast("decoder", new PacketDecoder(protocolType, maxFrameLength));
        pipeline.addLast("encoder", new PacketEncoder());

        // 消息处理
        pipeline.addLast("handler", new PacketHandler(messageDispatcher));
    }

    /**
     * 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SslContext sslContext;
        private String websocketPath = "/ws";
        private int maxFrameSize = 65536;
        private boolean enableCompression = true;
        private long readerIdleTime = 60;
        private long writerIdleTime = 0;
        private long allIdleTime = 0;
        private ProtocolType protocolType = ProtocolType.GATEWAY;
        private int maxFrameLength = 1024 * 1024;
        private MessageDispatcher messageDispatcher;

        public Builder sslContext(SslContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public Builder websocketPath(String websocketPath) {
            this.websocketPath = websocketPath;
            return this;
        }

        public Builder maxFrameSize(int maxFrameSize) {
            this.maxFrameSize = maxFrameSize;
            return this;
        }

        public Builder enableCompression(boolean enableCompression) {
            this.enableCompression = enableCompression;
            return this;
        }

        public Builder readerIdleTime(long readerIdleTime) {
            this.readerIdleTime = readerIdleTime;
            return this;
        }

        public Builder writerIdleTime(long writerIdleTime) {
            this.writerIdleTime = writerIdleTime;
            return this;
        }

        public Builder allIdleTime(long allIdleTime) {
            this.allIdleTime = allIdleTime;
            return this;
        }

        public Builder protocolType(ProtocolType protocolType) {
            this.protocolType = protocolType;
            return this;
        }

        public Builder maxFrameLength(int maxFrameLength) {
            this.maxFrameLength = maxFrameLength;
            return this;
        }

        public Builder messageDispatcher(MessageDispatcher messageDispatcher) {
            this.messageDispatcher = messageDispatcher;
            return this;
        }

        public WebSocketServerInitializer build() {
            return new WebSocketServerInitializer(
                    sslContext,
                    websocketPath,
                    maxFrameSize,
                    enableCompression,
                    readerIdleTime,
                    writerIdleTime,
                    allIdleTime,
                    protocolType,
                    maxFrameLength,
                    messageDispatcher
            );
        }
    }
}

