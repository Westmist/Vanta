package org.markeb.transport;

/**
 * RPC 传输类型
 */
public enum TransportType {

    /**
     * gRPC
     */
    GRPC,

    /**
     * 本地调用（用于测试）
     */
    LOCAL
}

