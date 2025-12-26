package org.markeb.net.transport;

/**
 * 传输层服务器接口
 */
public interface TransportServer {

    /**
     * 启动服务器
     */
    void start() throws Exception;

    /**
     * 停止服务器
     */
    void stop();

    /**
     * 获取传输类型
     */
    TransportType getTransportType();

    /**
     * 是否正在运行
     */
    boolean isRunning();

    /**
     * 获取监听端口
     */
    int getPort();
}

