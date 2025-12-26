package org.markeb.locate;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 玩家位置信息
 */
public class Location implements Serializable {

    /**
     * 玩家ID
     */
    private String playerId;

    /**
     * 服务器节点ID
     */
    private String nodeId;

    /**
     * 服务器地址
     */
    private String address;

    /**
     * 服务器端口
     */
    private int port;

    /**
     * 服务类型（如：game, chat, battle）
     */
    private String serverType;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;

    /**
     * 扩展数据
     */
    private String extra;

    public Location() {
    }

    public Location(String playerId, String nodeId, String address, int port) {
        this.playerId = playerId;
        this.nodeId = nodeId;
        this.address = address;
        this.port = port;
        this.lastUpdateTime = LocalDateTime.now();
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    /**
     * 获取完整地址
     */
    public String getFullAddress() {
        return address + ":" + port;
    }

    @Override
    public String toString() {
        return "Location{" +
                "playerId='" + playerId + '\'' +
                ", nodeId='" + nodeId + '\'' +
                ", address='" + address + '\'' +
                ", port=" + port +
                ", serverType='" + serverType + '\'' +
                '}';
    }
}

