# Transport Spring Boot Starter

服务间 RPC 通信，基于 gRPC。

## 功能特性

- **RPC 调用**：服务间同步/异步调用
- **负载均衡**：支持多种负载策略
- **服务发现**：自动发现目标服务

## 目录结构

```
transport/
├── config/          # 自动配置
├── TransportClient.java  # 传输客户端
├── TransportServer.java  # 传输服务端
└── loadbalance/     # 负载均衡策略
```

## 快速使用

```java
@Autowired
private TransportClient client;

// 发送请求
Response resp = client.send("target-service", request);

// 异步发送
CompletableFuture<Response> future = client.sendAsync("target-service", request);
```

## 配置项

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `markeb.transport.enabled` | `true` | 是否启用 |
| `markeb.transport.timeout` | `5000` | 超时时间(ms) |

