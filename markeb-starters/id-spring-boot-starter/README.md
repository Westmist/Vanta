# ID Spring Boot Starter

分布式 ID 生成器，基于雪花算法。

## 功能特性

- **全局唯一**：分布式环境下保证唯一
- **趋势递增**：ID 整体趋势递增
- **高性能**：单机每秒可生成百万级 ID

## 目录结构

```
id/
├── config/          # 自动配置
├── IdGenerator.java     # ID 生成器接口
├── YitIdGenerator.java  # 雪花算法实现
└── worker/          # Worker ID 分配
```

## 快速使用

```java
@Autowired
private IdGenerator idGenerator;

long id = idGenerator.nextId();
String strId = idGenerator.nextIdStr();
```

## 配置项

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `markeb.id.enabled` | `true` | 是否启用 |
| `markeb.id.worker-id` | 自动分配 | Worker ID (0-1023) |
| `markeb.id.datacenter-id` | `0` | 数据中心 ID |

