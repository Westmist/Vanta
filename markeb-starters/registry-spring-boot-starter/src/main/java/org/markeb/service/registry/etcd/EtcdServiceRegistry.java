package org.markeb.service.registry.etcd;

import org.markeb.service.registry.ServiceInstance;
import org.markeb.service.registry.ServiceRegistry;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Etcd 服务注册实现
 */
@Slf4j
public class EtcdServiceRegistry implements ServiceRegistry {

    private static final String SERVICE_PREFIX = "/services/";
    private static final long DEFAULT_TTL = 30; // 30 seconds
    private static final long KEEP_ALIVE_INTERVAL = 10; // 10 seconds

    private final Client client;
    private final KV kvClient;
    private final Lease leaseClient;
    private final long ttl;
    private final Map<String, Long> leaseIdMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;

    public EtcdServiceRegistry(Client client, long ttl) {
        this.client = client;
        this.kvClient = client.getKVClient();
        this.leaseClient = client.getLeaseClient();
        this.ttl = ttl > 0 ? ttl : DEFAULT_TTL;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "etcd-keepalive");
            t.setDaemon(true);
            return t;
        });
    }

    public EtcdServiceRegistry(Client client) {
        this(client, DEFAULT_TTL);
    }

    @Override
    public void register(ServiceInstance instance) {
        try {
            // 创建租约
            LeaseGrantResponse leaseResponse = leaseClient.grant(ttl).get();
            long leaseId = leaseResponse.getID();

            String key = buildKey(instance);
            String value = serializeInstance(instance);

            // 使用租约存储服务信息
            PutOption option = PutOption.builder().withLeaseId(leaseId).build();
            kvClient.put(
                    ByteSequence.from(key, StandardCharsets.UTF_8),
                    ByteSequence.from(value, StandardCharsets.UTF_8),
                    option
            ).get();

            leaseIdMap.put(key, leaseId);

            // 启动心跳保活
            startKeepAlive(leaseId, key);

            log.info("Etcd service registered: {} -> {}:{}", instance.getServiceName(),
                    instance.getHost(), instance.getPort());
        } catch (Exception e) {
            log.error("Failed to register service to Etcd: {}", instance.getServiceName(), e);
            throw new RuntimeException("Failed to register service to Etcd", e);
        }
    }

    private void startKeepAlive(long leaseId, String key) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (leaseIdMap.containsKey(key)) {
                    leaseClient.keepAliveOnce(leaseId).get();
                }
            } catch (Exception e) {
                log.warn("Failed to keep alive lease for key: {}", key, e);
            }
        }, KEEP_ALIVE_INTERVAL, KEEP_ALIVE_INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    public void deregister(ServiceInstance instance) {
        try {
            String key = buildKey(instance);
            kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            
            Long leaseId = leaseIdMap.remove(key);
            if (leaseId != null) {
                leaseClient.revoke(leaseId).get();
            }

            log.info("Etcd service deregistered: {} -> {}:{}", instance.getServiceName(),
                    instance.getHost(), instance.getPort());
        } catch (Exception e) {
            log.error("Failed to deregister service from Etcd: {}", instance.getServiceName(), e);
            throw new RuntimeException("Failed to deregister service from Etcd", e);
        }
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceName) {
        try {
            String prefix = SERVICE_PREFIX + serviceName + "/";
            GetOption option = GetOption.builder()
                    .isPrefix(true)
                    .build();

            GetResponse response = kvClient.get(
                    ByteSequence.from(prefix, StandardCharsets.UTF_8),
                    option
            ).get();

            return response.getKvs().stream()
                    .map(kv -> deserializeInstance(kv.getValue().toString(StandardCharsets.UTF_8)))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get instances from Etcd: {}", serviceName, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getServices() {
        try {
            GetOption option = GetOption.builder()
                    .isPrefix(true)
                    .withKeysOnly(true)
                    .build();

            GetResponse response = kvClient.get(
                    ByteSequence.from(SERVICE_PREFIX, StandardCharsets.UTF_8),
                    option
            ).get();

            return response.getKvs().stream()
                    .map(kv -> kv.getKey().toString(StandardCharsets.UTF_8))
                    .map(key -> key.substring(SERVICE_PREFIX.length()))
                    .map(key -> key.split("/")[0])
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get services from Etcd", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void close() {
        scheduler.shutdown();
        leaseIdMap.values().forEach(leaseId -> {
            try {
                leaseClient.revoke(leaseId).get();
            } catch (Exception e) {
                log.warn("Failed to revoke lease: {}", leaseId, e);
            }
        });
        leaseIdMap.clear();
        client.close();
        log.info("Etcd service registry closed");
    }

    private String buildKey(ServiceInstance instance) {
        return SERVICE_PREFIX + instance.getServiceName() + "/" + instance.getHost() + ":" + instance.getPort();
    }

    private String serializeInstance(ServiceInstance instance) {
        StringBuilder sb = new StringBuilder();
        sb.append(instance.getInstanceId()).append("|");
        sb.append(instance.getServiceName()).append("|");
        sb.append(instance.getHost()).append("|");
        sb.append(instance.getPort()).append("|");
        sb.append(instance.getWeight()).append("|");
        sb.append(instance.isHealthy()).append("|");
        sb.append(instance.isEnabled()).append("|");
        if (instance.getMetadata() != null) {
            instance.getMetadata().forEach((k, v) -> sb.append(k).append("=").append(v).append(","));
        }
        return sb.toString();
    }

    private ServiceInstance deserializeInstance(String value) {
        String[] parts = value.split("\\|");
        ServiceInstance.ServiceInstanceBuilder builder = ServiceInstance.builder()
                .instanceId(parts[0])
                .serviceName(parts[1])
                .host(parts[2])
                .port(Integer.parseInt(parts[3]))
                .weight(Double.parseDouble(parts[4]))
                .healthy(Boolean.parseBoolean(parts[5]))
                .enabled(Boolean.parseBoolean(parts[6]));

        if (parts.length > 7 && !parts[7].isEmpty()) {
            Map<String, String> metadata = new ConcurrentHashMap<>();
            String[] metaParts = parts[7].split(",");
            for (String metaPart : metaParts) {
                if (metaPart.contains("=")) {
                    String[] kv = metaPart.split("=", 2);
                    metadata.put(kv[0], kv[1]);
                }
            }
            builder.metadata(metadata);
        }

        return builder.build();
    }
}

