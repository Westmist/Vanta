package org.markeb.transport;

import java.util.concurrent.CompletableFuture;

/**
 * RPC 客户端接口
 */
public interface RpcClient {

    /**
     * 同步调用
     *
     * @param target  目标服务地址
     * @param service 服务名
     * @param method  方法名
     * @param request 请求数据
     * @return 响应数据
     */
    byte[] call(String target, String service, String method, byte[] request);

    /**
     * 异步调用
     *
     * @param target  目标服务地址
     * @param service 服务名
     * @param method  方法名
     * @param request 请求数据
     * @return 响应 Future
     */
    CompletableFuture<byte[]> callAsync(String target, String service, String method, byte[] request);

    /**
     * 关闭客户端
     */
    void shutdown();
}

