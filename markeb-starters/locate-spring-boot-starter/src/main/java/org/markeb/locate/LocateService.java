package org.markeb.locate;

import java.util.List;
import java.util.Optional;

/**
 * 玩家定位服务接口
 * 用于跨服通信时定位玩家所在的服务器节点
 */
public interface LocateService {

    /**
     * 绑定玩家位置
     *
     * @param playerId 玩家ID
     * @param location 位置信息
     */
    void bind(String playerId, Location location);

    /**
     * 解绑玩家位置
     *
     * @param playerId 玩家ID
     */
    void unbind(String playerId);

    /**
     * 获取玩家位置
     *
     * @param playerId 玩家ID
     * @return 位置信息
     */
    Optional<Location> locate(String playerId);

    /**
     * 批量获取玩家位置
     *
     * @param playerIds 玩家ID列表
     * @return 位置信息列表
     */
    List<Location> locateAll(List<String> playerIds);

    /**
     * 检查玩家是否在线
     *
     * @param playerId 玩家ID
     * @return 是否在线
     */
    boolean isOnline(String playerId);

    /**
     * 获取指定节点上的所有玩家
     *
     * @param nodeId 节点ID
     * @return 玩家ID列表
     */
    List<String> getPlayersByNode(String nodeId);

    /**
     * 获取在线玩家数量
     *
     * @return 在线玩家数
     */
    long getOnlineCount();

    /**
     * 更新玩家位置（心跳续期）
     *
     * @param playerId 玩家ID
     */
    void refresh(String playerId);
}

