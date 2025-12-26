package org.markeb.transport;

/**
 * RPC 请求处理器
 */
@FunctionalInterface
public interface RpcHandler {

    /**
     * 处理 RPC 请求
     *
     * @param method  方法名
     * @param request 请求数据
     * @return 响应数据
     */
    byte[] handle(String method, byte[] request);
}

