# Locate Spring Boot Starter

玩家定位服务，快速查找玩家所在服务器节点。

## 功能特性

- **玩家定位**：记录玩家当前所在节点
- **多后端**：Redis / 本地缓存
- **自动过期**：支持 TTL 自动清理

## 目录结构

```
locate/
├── config/          # 自动配置
├── LocateService.java   # 定位服务接口
├── Location.java        # 位置信息
├── redis/           # Redis 实现
└── local/           # 本地缓存实现
```

## 快速使用

```java
@Autowired
private LocateService locateService;

// 注册位置
locateService.register("player_123", "node-1", 8000);

// 查找玩家
Location loc = locateService.locate("player_123");
```

## 配置项

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `markeb.locate.enabled` | `true` | 是否启用 |
| `markeb.locate.type` | `REDIS` | 类型：REDIS/LOCAL |
| `markeb.locate.expire-time` | `30m` | 过期时间 |

