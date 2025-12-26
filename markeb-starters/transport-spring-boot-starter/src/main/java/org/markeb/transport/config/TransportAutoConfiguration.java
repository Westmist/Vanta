package org.markeb.transport.config;

import org.markeb.transport.RpcClient;
import org.markeb.transport.RpcServer;
import org.markeb.transport.grpc.GrpcRpcClient;
import org.markeb.transport.grpc.GrpcRpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RPC 传输自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(TransportProperties.class)
@ConditionalOnProperty(prefix = "markeb.transport", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TransportAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TransportAutoConfiguration.class);

    @Configuration
    @ConditionalOnClass(name = "io.grpc.ManagedChannel")
    @ConditionalOnProperty(prefix = "markeb.transport", name = "type", havingValue = "GRPC", matchIfMissing = true)
    static class GrpcConfiguration {

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(prefix = "markeb.transport.server", name = "enabled", havingValue = "true", matchIfMissing = true)
        public RpcServer grpcRpcServer(TransportProperties properties) {
            log.info("Initializing gRPC server on port {}", properties.getServer().getPort());
            GrpcRpcServer server = new GrpcRpcServer(properties.getServer().getPort());
            server.start();
            return server;
        }

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnProperty(prefix = "markeb.transport.client", name = "enabled", havingValue = "true", matchIfMissing = true)
        public RpcClient grpcRpcClient(TransportProperties properties) {
            log.info("Initializing gRPC client with timeout {}ms", properties.getClient().getTimeout());
            return new GrpcRpcClient(properties.getClient().getTimeout());
        }
    }
}

