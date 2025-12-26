package org.markeb.mesh.match;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * 匹配服务
 */
@Service
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);

    /**
     * 匹配队列（gameMode -> 请求队列）
     */
    private final Map<String, Queue<MatchRequest>> matchQueues = new ConcurrentHashMap<>();

    /**
     * 玩家请求映射（playerId -> request）
     */
    private final Map<String, MatchRequest> playerRequests = new ConcurrentHashMap<>();

    /**
     * 匹配结果回调
     */
    private final Map<String, CompletableFuture<MatchResult>> pendingMatches = new ConcurrentHashMap<>();

    /**
     * 匹配执行器
     */
    private final ScheduledExecutorService matchExecutor = Executors.newScheduledThreadPool(2);

    /**
     * 匹配配置
     */
    private final MatchConfig config = new MatchConfig();

    public MatchService() {
        // 启动匹配任务
        matchExecutor.scheduleAtFixedRate(this::processMatching, 1, 1, TimeUnit.SECONDS);
        // 启动超时检查任务
        matchExecutor.scheduleAtFixedRate(this::checkTimeout, 5, 5, TimeUnit.SECONDS);
    }

    /**
     * 提交匹配请求
     */
    public CompletableFuture<MatchResult> submitRequest(MatchRequest request) {
        // 检查是否已在匹配中
        if (playerRequests.containsKey(request.getPlayerId())) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Player already in matching: " + request.getPlayerId()));
        }

        request.setRequestId(UUID.randomUUID().toString());
        request.setStatus(MatchRequest.MatchStatus.WAITING);

        // 加入队列
        Queue<MatchRequest> queue = matchQueues.computeIfAbsent(
                request.getGameMode(), k -> new ConcurrentLinkedQueue<>());
        queue.offer(request);
        playerRequests.put(request.getPlayerId(), request);

        // 创建结果 Future
        CompletableFuture<MatchResult> future = new CompletableFuture<>();
        pendingMatches.put(request.getRequestId(), future);

        log.info("Player {} submitted match request for mode {}", 
                request.getPlayerId(), request.getGameMode());

        return future;
    }

    /**
     * 取消匹配
     */
    public boolean cancelRequest(String playerId) {
        MatchRequest request = playerRequests.remove(playerId);
        if (request == null) {
            return false;
        }

        request.setStatus(MatchRequest.MatchStatus.CANCELLED);

        // 从队列移除
        Queue<MatchRequest> queue = matchQueues.get(request.getGameMode());
        if (queue != null) {
            queue.remove(request);
        }

        // 完成 Future
        CompletableFuture<MatchResult> future = pendingMatches.remove(request.getRequestId());
        if (future != null) {
            future.cancel(false);
        }

        log.info("Player {} cancelled match request", playerId);
        return true;
    }

    /**
     * 处理匹配逻辑
     */
    private void processMatching() {
        for (Map.Entry<String, Queue<MatchRequest>> entry : matchQueues.entrySet()) {
            String gameMode = entry.getKey();
            Queue<MatchRequest> queue = entry.getValue();

            int requiredPlayers = config.getRequiredPlayers(gameMode);
            if (queue.size() < requiredPlayers) {
                continue;
            }

            // 尝试匹配
            List<MatchRequest> candidates = new ArrayList<>();
            for (MatchRequest request : queue) {
                if (request.getStatus() == MatchRequest.MatchStatus.WAITING) {
                    candidates.add(request);
                    if (candidates.size() >= requiredPlayers) {
                        break;
                    }
                }
            }

            if (candidates.size() >= requiredPlayers) {
                // 匹配成功
                createMatch(gameMode, candidates);
            }
        }
    }

    /**
     * 创建匹配
     */
    private void createMatch(String gameMode, List<MatchRequest> requests) {
        String matchId = UUID.randomUUID().toString();
        List<String> playerIds = new ArrayList<>();

        for (MatchRequest request : requests) {
            request.setStatus(MatchRequest.MatchStatus.MATCHED);
            playerIds.add(request.getPlayerId());

            // 从队列和映射中移除
            Queue<MatchRequest> queue = matchQueues.get(gameMode);
            if (queue != null) {
                queue.remove(request);
            }
            playerRequests.remove(request.getPlayerId());
        }

        // 创建匹配结果
        MatchResult result = new MatchResult(matchId, gameMode, playerIds);
        result.setRoomId("room_" + matchId.substring(0, 8));

        // TODO: 从服务注册中心选择一个游戏节点
        result.setNodeId("node-1");
        result.setNodeAddress("127.0.0.1:9000");

        log.info("Match created: {} with players {}", matchId, playerIds);

        // 通知所有玩家
        for (MatchRequest request : requests) {
            CompletableFuture<MatchResult> future = pendingMatches.remove(request.getRequestId());
            if (future != null) {
                future.complete(result);
            }
        }
    }

    /**
     * 检查超时请求
     */
    private void checkTimeout() {
        LocalDateTime now = LocalDateTime.now();
        Duration timeout = config.getMatchTimeout();

        for (Map.Entry<String, MatchRequest> entry : playerRequests.entrySet()) {
            MatchRequest request = entry.getValue();
            if (request.getStatus() == MatchRequest.MatchStatus.WAITING) {
                Duration waiting = Duration.between(request.getRequestTime(), now);
                if (waiting.compareTo(timeout) > 0) {
                    // 超时
                    request.setStatus(MatchRequest.MatchStatus.TIMEOUT);
                    playerRequests.remove(entry.getKey());

                    Queue<MatchRequest> queue = matchQueues.get(request.getGameMode());
                    if (queue != null) {
                        queue.remove(request);
                    }

                    CompletableFuture<MatchResult> future = pendingMatches.remove(request.getRequestId());
                    if (future != null) {
                        future.completeExceptionally(new TimeoutException("Match timeout"));
                    }

                    log.info("Player {} match request timeout", request.getPlayerId());
                }
            }
        }
    }

    /**
     * 获取队列中的玩家数
     */
    public int getQueueSize(String gameMode) {
        Queue<MatchRequest> queue = matchQueues.get(gameMode);
        return queue != null ? queue.size() : 0;
    }

    /**
     * 匹配配置
     */
    public static class MatchConfig {
        private final Map<String, Integer> requiredPlayers = new HashMap<>() {{
            put("1v1", 2);
            put("2v2", 4);
            put("5v5", 10);
            put("battle_royale", 100);
        }};

        private Duration matchTimeout = Duration.ofMinutes(3);

        public int getRequiredPlayers(String gameMode) {
            return requiredPlayers.getOrDefault(gameMode, 2);
        }

        public Duration getMatchTimeout() {
            return matchTimeout;
        }

        public void setMatchTimeout(Duration matchTimeout) {
            this.matchTimeout = matchTimeout;
        }
    }
}

