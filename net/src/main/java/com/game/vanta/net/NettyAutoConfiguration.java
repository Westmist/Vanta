package com.game.vanta.net;

import com.game.vanta.net.handler.ChannelInitializerProvider;
import com.game.vanta.net.handler.DefaultChannelInitializer;
import com.game.vanta.net.handler.IBusinessChannelHandler;
import com.game.vanta.net.msg.IGameParser;
import com.game.vanta.net.msg.IMessagePool;
import com.game.vanta.net.msg.ProtoBuffGameMessagePool;
import com.game.vanta.net.msg.ProtoBuffParser;
import com.game.vanta.net.register.MessageHandlerRegistrar;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(NettyProperties.class)
public class NettyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IGameParser.class)
    public IGameParser<?> gameParser() {
        return new ProtoBuffParser();
    }

    @Bean
    @ConditionalOnMissingBean(IMessagePool.class)
    public IMessagePool<?> messagePool(IGameParser<?> gameParser) {
        return new ProtoBuffGameMessagePool(gameParser);
    }

    @Bean
    public MessageHandlerRegistrar messageHandlerRegistrar() {
        return new MessageHandlerRegistrar();
    }

    @Bean
    @ConditionalOnMissingBean(ChannelInitializerProvider.class)
    public ChannelInitializerProvider channelInitializerProvider(
            IMessagePool<?> iMessagePool,
            ObjectProvider<List<IBusinessChannelHandler>> businessHandlers) {
        return new DefaultChannelInitializer(iMessagePool, businessHandlers);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public NettyServer nettyServer(NettyProperties properties, ChannelInitializerProvider initializerProvider) {
        return new NettyServer(properties, initializerProvider);
    }

}
