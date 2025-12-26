package org.markeb.gateway.route;

import org.markeb.gateway.session.GatewaySession;
import org.markeb.service.registry.ServiceInstance;
import org.markeb.service.registry.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 节点路由器
 * 负责将请求路由到合适的游戏节点
 */
@Component
public class NodeRouter {

    private static final Logger log = LoggerFactory.getLogger(NodeRouter.class);

    private static final String NODE_SERVICE_NAME = "markeb-node";

    @Autowired(required = false)
    private ServiceRegistry serviceRegistry;

    /**
     * 静态节点配置（当没有服务注册时使用）
     */
    private final Map<String, String> staticNodes = new ConcurrentHashMap<>();

    /**
     * 轮询计数器（用于负载均衡）
     */
    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

    /**
     * 路由策略
     */
    public enum RouteStrategy {
        /**
         * 轮询
         */
        ROUND_ROBIN,

        /**
         * 随机
         */
        RANDOM,

        /**
         * 一致性哈希（根据玩家ID）
         */
        CONSISTENT_HASH,

        /**
         * 指定节点
         */
        DESIGNATED
    }

    /**
     * 为会话选择节点
     *
     * @param session  网关会话
     * @param strategy 路由策略
     * @return 节点地址 (host:port)
     */
    public Optional<String> selectNode(GatewaySession session, RouteStrategy strategy) {
        // 如果会话已绑定节点，直接返回
        if (session.getNodeId() != null) {
            return getNodeAddress(session.getNodeId());
        }

        // 获取可用节点列表
        List<ServiceInstance> nodes = getAvailableNodes();
        if (nodes.isEmpty()) {
            // 尝试使用静态配置
            if (!staticNodes.isEmpty()) {
                return selectFromStaticNodes(session, strategy);
            }
            log.warn("No available nodes for routing");
            return Optional.empty();
        }

        // 根据策略选择节点
        ServiceInstance selected = switch (strategy) {
            case ROUND_ROBIN -> selectByRoundRobin(nodes);
            case RANDOM -> selectByRandom(nodes);
            case CONSISTENT_HASH -> selectByConsistentHash(nodes, session.getPlayerId());
            case DESIGNATED -> nodes.get(0); // 默认选第一个
        };

        if (selected != null) {
            String nodeId = selected.getInstanceId();
            session.setNodeId(nodeId);
            return Optional.of(selected.getHost() + ":" + selected.getPort());
        }

        return Optional.empty();
    }

    /**
     * 获取指定节点的地址
     */
    public Optional<String> getNodeAddress(String nodeId) {
        // 先从服务注册中心查找
        if (serviceRegistry != null) {
            List<ServiceInstance> instances = serviceRegistry.getInstances(NODE_SERVICE_NAME);
            for (ServiceInstance instance : instances) {
                if (nodeId.equals(instance.getInstanceId())) {
                    return Optional.of(instance.getHost() + ":" + instance.getPort());
                }
            }
        }

        // 从静态配置查找
        String address = staticNodes.get(nodeId);
        return Optional.ofNullable(address);
    }

    /**
     * 获取可用节点列表
     */
    private List<ServiceInstance> getAvailableNodes() {
        if (serviceRegistry == null) {
            return List.of();
        }
        return serviceRegistry.getInstances(NODE_SERVICE_NAME);
    }

    /**
     * 轮询选择
     */
    private ServiceInstance selectByRoundRobin(List<ServiceInstance> nodes) {
        int index = Math.abs(roundRobinCounter.getAndIncrement() % nodes.size());
        return nodes.get(index);
    }

    /**
     * 随机选择
     */
    private ServiceInstance selectByRandom(List<ServiceInstance> nodes) {
        int index = (int) (Math.random() * nodes.size());
        return nodes.get(index);
    }

    /**
     * 一致性哈希选择
     */
    private ServiceInstance selectByConsistentHash(List<ServiceInstance> nodes, String playerId) {
        if (playerId == null) {
            return selectByRoundRobin(nodes);
        }
        int hash = Math.abs(playerId.hashCode());
        int index = hash % nodes.size();
        return nodes.get(index);
    }

    /**
     * 从静态配置选择节点
     */
    private Optional<String> selectFromStaticNodes(GatewaySession session, RouteStrategy strategy) {
        if (staticNodes.isEmpty()) {
            return Optional.empty();
        }

        List<String> nodeIds = List.copyOf(staticNodes.keySet());
        String selectedId = switch (strategy) {
            case ROUND_ROBIN -> {
                int index = Math.abs(roundRobinCounter.getAndIncrement() % nodeIds.size());
                yield nodeIds.get(index);
            }
            case RANDOM -> {
                int index = (int) (Math.random() * nodeIds.size());
                yield nodeIds.get(index);
            }
            case CONSISTENT_HASH -> {
                if (session.getPlayerId() == null) {
                    yield nodeIds.get(0);
                }
                int hash = Math.abs(session.getPlayerId().hashCode());
                yield nodeIds.get(hash % nodeIds.size());
            }
            case DESIGNATED -> nodeIds.get(0);
        };

        session.setNodeId(selectedId);
        return Optional.ofNullable(staticNodes.get(selectedId));
    }

    /**
     * 添加静态节点配置
     */
    public void addStaticNode(String nodeId, String address) {
        staticNodes.put(nodeId, address);
        log.info("Added static node: {} -> {}", nodeId, address);
    }

    /**
     * 移除静态节点配置
     */
    public void removeStaticNode(String nodeId) {
        staticNodes.remove(nodeId);
    }
}

