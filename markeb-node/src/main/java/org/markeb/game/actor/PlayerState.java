package org.markeb.game.actor;

/**
 * 玩家状态
 * <p>
 * 存储玩家的运行时状态数据。
 * 这个对象在 Actor 内部是线程安全的，因为所有操作都是串行的。
 * </p>
 */
public class PlayerState {

    private long playerId;
    private String nickname;
    private int level;
    private long experience;
    private long gold;
    private long lastLoginTime;
    private long lastLogoutTime;

    public PlayerState() {
    }

    public PlayerState(long playerId) {
        this.playerId = playerId;
        this.level = 1;
        this.experience = 0;
        this.gold = 0;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getExperience() {
        return experience;
    }

    public void setExperience(long experience) {
        this.experience = experience;
    }

    public void addExperience(long exp) {
        this.experience += exp;
        // 简单的升级逻辑
        while (this.experience >= getExpForNextLevel()) {
            this.experience -= getExpForNextLevel();
            this.level++;
        }
    }

    private long getExpForNextLevel() {
        return level * 100L;
    }

    public long getGold() {
        return gold;
    }

    public void setGold(long gold) {
        this.gold = gold;
    }

    public void addGold(long amount) {
        this.gold += amount;
    }

    public boolean deductGold(long amount) {
        if (this.gold >= amount) {
            this.gold -= amount;
            return true;
        }
        return false;
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public long getLastLogoutTime() {
        return lastLogoutTime;
    }

    public void setLastLogoutTime(long lastLogoutTime) {
        this.lastLogoutTime = lastLogoutTime;
    }

    @Override
    public String toString() {
        return "PlayerState{" +
                "playerId=" + playerId +
                ", nickname='" + nickname + '\'' +
                ", level=" + level +
                ", experience=" + experience +
                ", gold=" + gold +
                '}';
    }

}

