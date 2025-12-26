# Network Spring Boot Starter

基于 Netty 的高性能网络层框架，支持 TCP/KCP 传输和多种序列化协议。

## 功能特性

- **多传输协议**：TCP / KCP
- **多序列化**：Protobuf / Protostuff / JSON
- **消息分发**：自动扫描注册 Handler
- **网关支持**：内置网关编解码器

## 目录结构

```
net/
├── annotation/      # 注解定义
├── codec/           # Protobuf 编解码器
├── config/          # 自动配置
├── gateway/         # 网关相关（编解码、后端连接）
├── handler/         # 消息处理和分发
├── msg/             # 消息池和解析器
├── netty/           # Netty 服务器封装
├── protocol/        # 协议包定义
├── register/        # Handler 注册机制
├── serialization/   # 序列化实现
└── transport/       # 传输层（TCP/KCP）
```

## 快速使用

```java
@SpringBootApplication
@EnableMessageHandlerScan(
    handlerPackages = "com.example.handler",
    messagePackages = "com.example.proto")
public class Application { }

@Component
public class MyHandler {
    @MessageHandler
    public Response handle(Player player, Request req) {
        return Response.newBuilder().build();
    }
}
```

## 配置项

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `network.port` | `8000` | 监听端口 |
| `markeb.network.transport` | `TCP` | 传输类型：TCP/KCP |
| `markeb.network.codec` | `PROTOBUF` | 序列化：PROTOBUF/PROTOSTUFF/JSON |
| `markeb.network.protocol` | `GAME_SERVER` | 协议类型 |

