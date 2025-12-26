# Actor Spring Boot Starter

轻量级 Actor 模型实现，支持虚拟线程和平台线程双模式。

## 功能特性

- **消息串行化**：同一 Actor 的消息保证串行处理
- **双执行模式**：虚拟线程（推荐）/ 平台线程可切换
- **邮箱机制**：支持无界/有界邮箱
- **Ask 模式**：支持请求-响应模式

## 目录结构

```
actor/
├── config/          # 自动配置和属性类
├── executor/        # 执行器实现（虚拟线程/平台线程）
├── impl/            # Actor 核心实现
├── mailbox/         # 邮箱实现
├── message/         # 消息类型定义
└── game/            # 游戏场景封装（Player Actor）
```

## 快速使用

```java
@Autowired
private ActorSystem actorSystem;

// 创建 Actor
ActorRef player = actorSystem.spawn("player_1", new PlayerState(),
    (ctx, state, msg) -> {
        // 处理消息
        return state;
    });

// 发送消息
player.tell(message);
```

## 配置项

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `markeb.actor.enabled` | `true` | 是否启用 |
| `markeb.actor.executor-type` | `VIRTUAL` | 执行器类型：VIRTUAL/PLATFORM |
| `markeb.actor.parallelism` | CPU核心数 | 平台线程并行度 |
| `markeb.actor.system-name` | 自动获取 | 系统名称（默认取 spring.application.name） |

**零配置即可使用，所有配置项都有合理默认值。**

