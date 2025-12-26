# Config Spring Boot Starter

配置中心客户端，支持 Nacos / 本地文件。

## 功能特性

- **动态配置**：运行时热更新
- **多数据源**：Nacos / 本地文件
- **变更通知**：配置变更事件回调

## 目录结构

```
config/
├── center/          # 配置中心实现
├── ConfigService.java   # 配置服务接口
├── ConfigChangeEvent.java  # 变更事件
├── nacos/           # Nacos 实现
└── local/           # 本地文件实现
```

## 快速使用

```java
@Autowired
private ConfigService configService;

// 获取配置
String value = configService.getConfig("game.config");

// 监听变更
configService.addListener("game.config", event -> {
    String newValue = event.getNewValue();
});
```

## 配置项

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `markeb.config.enabled` | `true` | 是否启用 |
| `markeb.config.type` | `NACOS` | 类型：NACOS/LOCAL |

