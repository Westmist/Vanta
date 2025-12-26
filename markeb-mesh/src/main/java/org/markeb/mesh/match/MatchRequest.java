package org.markeb.mesh.match;

import java.time.LocalDateTime;

/**
 * 匹配请求
 */
public class MatchRequest {

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 玩家ID
     */
    private String playerId;

    /**
     * 游戏模式
     */
    private String gameMode;

    /**
     * 玩家评分（用于匹配）
     */
    private int rating;

    /**
     * 请求时间
     */
    private LocalDateTime requestTime;

    /**
     * 匹配状态
     */
    private MatchStatus status = MatchStatus.WAITING;

    public MatchRequest() {
        this.requestTime = LocalDateTime.now();
    }

    public MatchRequest(String playerId, String gameMode, int rating) {
        this();
        this.playerId = playerId;
        this.gameMode = gameMode;
        this.rating = rating;
    }

    // Getters and Setters

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public enum MatchStatus {
        /**
         * 等待匹配
         */
        WAITING,

        /**
         * 匹配中
         */
        MATCHING,

        /**
         * 匹配成功
         */
        MATCHED,

        /**
         * 已取消
         */
        CANCELLED,

        /**
         * 超时
         */
        TIMEOUT
    }
}

