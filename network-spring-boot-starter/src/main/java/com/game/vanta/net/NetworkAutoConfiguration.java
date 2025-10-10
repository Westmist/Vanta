package com.game.vanta.net;

import com.game.vanta.net.handler.ChannelInitializerProvider;
import com.game.vanta.net.handler.DefaultChannelInitializer;
import com.game.vanta.net.msg.IGameParser;
import com.game.vanta.net.msg.IMessagePool;
import com.game.vanta.net.msg.ProtoBuffGameMessagePool;
import com.game.vanta.net.msg.ProtoBuffParser;
import com.game.vanta.net.netty.BusinessHandlerProvider;
import com.game.vanta.net.netty.NettyProperties;
import com.game.vanta.net.netty.NettyServer;
import com.game.vanta.net.register.MessageHandlerRegistrar;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({NettyProperties.class, NetworkProperties.class})
public class NetworkAutoConfiguration {

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
    @ConditionalOnBean(BusinessHandlerProvider.class)
    @ConditionalOnMissingBean(ChannelInitializerProvider.class)
    public ChannelInitializerProvider channelInitializerProvider(
            IMessagePool<?> iMessagePool,
            BusinessHandlerProvider handlerProvider) {
        return new DefaultChannelInitializer(iMessagePool, handlerProvider);
    }

    @ConditionalOnMissingBean(INetworkServer.class)
    @ConditionalOnBean(ChannelInitializerProvider.class)
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(name = "network.enabled", havingValue = "true", matchIfMissing = true)
    public NettyServer nettyServer(
            NetworkProperties networkProperties,
            NettyProperties nettyProperties,
            ChannelInitializerProvider initializerProvider) {
        return new NettyServer(networkProperties, nettyProperties, initializerProvider);
    }

}
