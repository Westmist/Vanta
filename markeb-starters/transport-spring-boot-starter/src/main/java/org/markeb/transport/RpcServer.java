package org.markeb.transport;

/**
 * RPC 服务端接口
 */
public interface RpcServer {

    /**
     * 启动服务
     */
    void start();

    /**
     * 停止服务
     */
    void stop();

    /**
     * 注册服务处理器
     *
     * @param service 服务名
     * @param handler 处理器
     */
    void registerHandler(String service, RpcHandler handler);

    /**
     * 获取监听端口
     */
    int getPort();

    /**
     * 是否正在运行
     */
    boolean isRunning();
}

