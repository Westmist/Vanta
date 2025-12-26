# Registry Spring Boot Starter

服务注册与发现，支持 Nacos / Etcd / Consul。

## 功能特性

- **多注册中心**：Nacos / Etcd / Consul
- **自动注册**：启动时自动注册，关闭时自动注销
- **服务发现**：获取服务实例列表
- **健康检查**：支持心跳保活

## 目录结构

```
registry/
├── config/          # 自动配置（Nacos/Etcd/Consul）
├── ServiceRegistry.java      # 注册接口
├── ServiceDiscovery.java     # 发现接口
├── ServiceInstance.java      # 服务实例
└── impl/            # 各注册中心实现
```

## 快速使用

```java
@Autowired
private ServiceDiscovery discovery;

// 获取服务实例
List<ServiceInstance> instances = discovery.getInstances("user-service");
```

## 配置项

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `markeb.registry.enabled` | `true` | 是否启用 |
| `markeb.registry.type` | `NACOS` | 类型：NACOS/ETCD/CONSUL |
| `markeb.registry.service-name` | 自动获取 | 服务名（默认取 spring.application.name） |
| `markeb.registry.port` | 自动获取 | 端口（默认取 network.port） |
| `markeb.registry.nacos.server-addr` | `127.0.0.1:8848` | Nacos 地址 |

