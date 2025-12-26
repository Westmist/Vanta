package org.markeb.mesh.match;

import java.util.List;

/**
 * 匹配结果
 */
public class MatchResult {

    /**
     * 匹配ID
     */
    private String matchId;

    /**
     * 游戏模式
     */
    private String gameMode;

    /**
     * 匹配到的玩家列表
     */
    private List<String> playerIds;

    /**
     * 分配的游戏节点
     */
    private String nodeId;

    /**
     * 节点地址
     */
    private String nodeAddress;

    /**
     * 房间/战斗ID
     */
    private String roomId;

    public MatchResult() {
    }

    public MatchResult(String matchId, String gameMode, List<String> playerIds) {
        this.matchId = matchId;
        this.gameMode = gameMode;
        this.playerIds = playerIds;
    }

    // Getters and Setters

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public List<String> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<String> playerIds) {
        this.playerIds = playerIds;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}

