package org.markeb.gateway.session;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 会话管理器
 * 管理所有客户端到网关的会话
 */
@Component
public class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    /**
     * sessionId -> GatewaySession
     */
    private final Map<Integer, GatewaySession> sessions = new ConcurrentHashMap<>();

    /**
     * playerId -> sessionId（用于通过玩家ID快速查找会话）
     */
    private final Map<String, Integer> playerSessions = new ConcurrentHashMap<>();

    /**
     * channelId -> sessionId
     */
    private final Map<String, Integer> channelSessions = new ConcurrentHashMap<>();

    /**
     * sessionId 生成器
     */
    private final AtomicInteger sessionIdGenerator = new AtomicInteger(0);

    /**
     * 创建新会话
     */
    public GatewaySession createSession(Channel channel) {
        int sessionId = sessionIdGenerator.incrementAndGet();
        GatewaySession session = new GatewaySession(sessionId, channel);

        sessions.put(sessionId, session);
        channelSessions.put(channel.id().asLongText(), sessionId);

        log.info("Created session: {}", session);
        return session;
    }

    /**
     * 绑定玩家到会话
     */
    public void bindPlayer(int sessionId, String playerId) {
        GatewaySession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("Session not found: {}", sessionId);
            return;
        }

        // 检查是否已有该玩家的会话（踢掉旧连接）
        Integer oldSessionId = playerSessions.get(playerId);
        if (oldSessionId != null && oldSessionId != sessionId) {
            GatewaySession oldSession = sessions.get(oldSessionId);
            if (oldSession != null) {
                log.info("Kicking old session for player {}: {}", playerId, oldSessionId);
                oldSession.close();
                removeSession(oldSessionId);
            }
        }

        session.setPlayerId(playerId);
        session.setState(GatewaySession.SessionState.AUTHENTICATED);
        playerSessions.put(playerId, sessionId);

        log.info("Bound player {} to session {}", playerId, sessionId);
    }

    /**
     * 绑定节点到会话
     */
    public void bindNode(int sessionId, String nodeId) {
        GatewaySession session = sessions.get(sessionId);
        if (session != null) {
            session.setNodeId(nodeId);
            log.debug("Bound node {} to session {}", nodeId, sessionId);
        }
    }

    /**
     * 获取会话
     */
    public Optional<GatewaySession> getSession(int sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * 通过 Channel 获取会话
     */
    public Optional<GatewaySession> getSessionByChannel(Channel channel) {
        Integer sessionId = channelSessions.get(channel.id().asLongText());
        if (sessionId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * 通过玩家ID获取会话
     */
    public Optional<GatewaySession> getSessionByPlayerId(String playerId) {
        Integer sessionId = playerSessions.get(playerId);
        if (sessionId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * 移除会话
     */
    public void removeSession(int sessionId) {
        GatewaySession session = sessions.remove(sessionId);
        if (session != null) {
            if (session.getPlayerId() != null) {
                playerSessions.remove(session.getPlayerId());
            }
            if (session.getFrontendChannel() != null) {
                channelSessions.remove(session.getFrontendChannel().id().asLongText());
            }
            log.info("Removed session: {}", session);
        }
    }

    /**
     * 通过 Channel 移除会话
     */
    public void removeSessionByChannel(Channel channel) {
        Integer sessionId = channelSessions.get(channel.id().asLongText());
        if (sessionId != null) {
            removeSession(sessionId);
        }
    }

    /**
     * 获取所有会话
     */
    public Collection<GatewaySession> getAllSessions() {
        return sessions.values();
    }

    /**
     * 获取在线会话数
     */
    public int getSessionCount() {
        return sessions.size();
    }

    /**
     * 获取已认证的会话数
     */
    public long getAuthenticatedCount() {
        return sessions.values().stream()
                .filter(GatewaySession::isAuthenticated)
                .count();
    }

    /**
     * 向指定玩家发送消息
     */
    public boolean sendToPlayer(String playerId, Object msg) {
        return getSessionByPlayerId(playerId)
                .filter(GatewaySession::isActive)
                .map(session -> {
                    session.send(msg);
                    return true;
                })
                .orElse(false);
    }

    /**
     * 广播消息给所有已认证的玩家
     */
    public void broadcast(Object msg) {
        sessions.values().stream()
                .filter(GatewaySession::isAuthenticated)
                .filter(GatewaySession::isActive)
                .forEach(session -> session.send(msg));
    }
}

