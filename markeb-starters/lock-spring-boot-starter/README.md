# Lock Spring Boot Starter

分布式锁，支持 Redis / Etcd。

## 功能特性

- **多后端**：Redis (Redisson) / Etcd
- **注解支持**：`@DistributedLock` 声明式加锁
- **可重入**：支持可重入锁
- **自动续期**：防止业务超时锁失效

## 目录结构

```
lock/
├── config/          # 自动配置
├── annotation/      # @DistributedLock 注解
├── aspect/          # AOP 切面
├── DistributedLock.java  # 锁接口
├── redis/           # Redis 实现
└── local/           # 本地锁实现
```

## 快速使用

```java
// 注解方式
@DistributedLock(key = "'player:' + #playerId")
public void doSomething(String playerId) { }

// 编程方式
@Autowired
private DistributedLock lock;

if (lock.tryLock("my-key", 10, TimeUnit.SECONDS)) {
    try {
        // 业务逻辑
    } finally {
        lock.unlock("my-key");
    }
}
```

## 配置项

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `markeb.lock.enabled` | `true` | 是否启用 |
| `markeb.lock.type` | `REDIS` | 类型：REDIS/ETCD/LOCAL |

