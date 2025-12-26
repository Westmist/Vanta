# EventBus Spring Boot Starter

分布式事件总线，支持 Redis / RocketMQ / Kafka。

## 功能特性

- **多后端**：Redis Pub/Sub / RocketMQ / Kafka
- **注解驱动**：`@EventListener` 自动订阅
- **跨服通信**：支持跨进程事件传递

## 目录结构

```
eventbus/
├── config/          # 自动配置
├── annotation/      # @EventListener 注解
├── redis/           # Redis 发布订阅实现
├── rocketmq/        # RocketMQ 实现
├── kafka/           # Kafka 实现
└── serialization/   # 事件序列化
```

## 快速使用

```java
// 定义事件
public class PlayerLoginEvent implements Event {
    public String topic() { return "player.login"; }
}

// 发布事件
@Autowired
private EventPublisher publisher;
publisher.publish(new PlayerLoginEvent());

// 订阅事件
@Component
public class MyListener {
    @EventListener
    public void onLogin(PlayerLoginEvent event) { }
}
```

## 配置项

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `markeb.eventbus.enabled` | `true` | 是否启用 |
| `markeb.eventbus.type` | `REDIS` | 类型：REDIS/ROCKETMQ/KAFKA |

