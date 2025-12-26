# Persistent Spring Boot Starter

异步持久化框架，Redis 缓存 + MongoDB 存储 + RocketMQ 消息队列。

## 功能特性

- **二级缓存**：Redis 热数据 + MongoDB 持久化
- **异步落盘**：通过 MQ 异步写入数据库
- **批量操作**：支持批量保存提升性能
- **实体管理**：统一的数据实体生命周期

## 目录结构

```
persistent/
├── config/          # 自动配置
├── entity/          # 持久化实体基类
├── cache/           # Redis 缓存操作
├── repository/      # MongoDB 数据访问
├── mq/              # RocketMQ 生产者/消费者
└── manager/         # 数据管理器
```

## 快速使用

```java
@Data
public class PlayerData extends PersistentEntity {
    private String playerId;
    private int level;
}

@Autowired
private DataManager dataManager;

// 异步保存
dataManager.saveAsync(playerData);

// 查询
PlayerData data = dataManager.get(playerId, PlayerData.class);
```

## 配置项

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `markeb.persistent.topic` | - | RocketMQ Topic |
| `markeb.persistent.batch-size` | `100` | 批量大小 |
| `markeb.persistent.flush-interval` | `5000` | 刷盘间隔(ms) |

