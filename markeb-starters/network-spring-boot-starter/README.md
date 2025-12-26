# Network Spring Boot Starter

基于 Netty 的高性能网络层框架，支持 TCP/KCP/WebSocket 传输和多种序列化协议。

## 功能特性

- **多传输协议**：TCP / KCP / WebSocket
- **多序列化**：Protobuf / Protostuff / JSON
- **消息分发**：自动扫描注册 Handler
- **网关支持**：内置网关编解码器
- **WebSocket 特性**：支持 SSL/TLS、压缩、自定义路径

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
└── transport/       # 传输层（TCP/KCP/WebSocket）
    ├── tcp/         # TCP 传输实现
    ├── kcp/         # KCP 传输实现（可靠 UDP）
    └── websocket/   # WebSocket 传输实现
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

### 基础配置

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `markeb.network.enabled` | `true` | 是否启用网络模块 |
| `markeb.network.port` | `9200` | 监听端口 |
| `markeb.network.transport` | `TCP` | 传输类型：TCP / KCP / WEBSOCKET |
| `markeb.network.codec` | `PROTOBUF` | 序列化：PROTOBUF / PROTOSTUFF / JSON |
| `markeb.network.protocol` | `GATEWAY` | 协议类型：GATEWAY / GAME_SERVER |
| `markeb.network.maxFrameLength` | `1048576` | 最大帧长度（1MB） |

### Netty 配置

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `markeb.network.netty.bossThreads` | `CPU/2` | Boss 线程数 |
| `markeb.network.netty.workerThreads` | `CPU*2` | Worker 线程数 |
| `markeb.network.netty.readerIdleTime` | `60` | 读空闲时间（秒） |
| `markeb.network.netty.writerIdleTime` | `0` | 写空闲时间（秒） |
| `markeb.network.netty.allIdleTime` | `0` | 读写空闲时间（秒） |

### WebSocket 配置

| 配置 | 默认值 | 说明 |
|------|--------|------|
| `markeb.network.websocket.path` | `/ws` | WebSocket 路径 |
| `markeb.network.websocket.maxFrameSize` | `65536` | 最大帧大小（字节） |
| `markeb.network.websocket.enableCompression` | `true` | 是否启用压缩 |
| `markeb.network.websocket.sslEnabled` | `false` | 是否启用 SSL |
| `markeb.network.websocket.sslCertPath` | - | SSL 证书路径 |
| `markeb.network.websocket.sslKeyPath` | - | SSL 私钥路径 |
| `markeb.network.websocket.sslKeyPassword` | - | SSL 私钥密码 |
| `markeb.network.websocket.subprotocols` | - | 子协议（可选） |

## 配置示例

### TCP 模式（默认）

```yaml
markeb:
  network:
    port: 9200
    transport: TCP
    codec: PROTOBUF
    protocol: GATEWAY
```

### WebSocket 模式

```yaml
markeb:
  network:
    port: 8080
    transport: WEBSOCKET
    codec: PROTOBUF
    protocol: GATEWAY
    websocket:
      path: /game
      enableCompression: true
```

### WebSocket + SSL 模式

```yaml
markeb:
  network:
    port: 443
    transport: WEBSOCKET
    websocket:
      path: /game
      sslEnabled: true
      sslCertPath: /path/to/cert.pem
      sslKeyPath: /path/to/key.pem
```

### KCP 模式（可靠 UDP）

```yaml
markeb:
  network:
    port: 9200
    transport: KCP
    codec: PROTOBUF
```

## 协议格式

### 网关协议（GATEWAY）

```
+--------+----------+-----+----------+------+
| length | messageId| seq | magicNum | body |
| 4 bytes| 4 bytes  | 2B  | 2 bytes  | n B  |
+--------+----------+-----+----------+------+
```

总协议头长度：12 bytes

### 游戏服协议（GAME_SERVER）

```
+--------+----------+-----+--------+--------+-------+------+
| length | messageId| seq | gateId | roleId | conId | body |
| 4 bytes| 4 bytes  | 2B  | 2 bytes| 8 bytes| 8 B   | n B  |
+--------+----------+-----+--------+--------+-------+------+
```

总协议头长度：28 bytes

## WebSocket 客户端连接示例

### JavaScript

```javascript
const ws = new WebSocket('ws://localhost:8080/game');
ws.binaryType = 'arraybuffer';

ws.onopen = () => {
    console.log('Connected');
    // 发送二进制数据
    const buffer = new ArrayBuffer(16);
    // ... 填充协议数据
    ws.send(buffer);
};

ws.onmessage = (event) => {
    const data = new DataView(event.data);
    // 解析协议
};
```

### Unity C#

```csharp
using WebSocketSharp;

var ws = new WebSocket("ws://localhost:8080/game");
ws.OnMessage += (sender, e) => {
    byte[] data = e.RawData;
    // 解析协议
};
ws.Connect();
```
