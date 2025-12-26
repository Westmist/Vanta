# markeb Gateway

游戏网关服务，负责客户端连接管理和消息路由。

## 功能特性

- **连接管理**：管理客户端 TCP/WebSocket 连接
- **消息路由**：将消息转发到后端游戏节点
- **会话管理**：维护客户端会话状态
- **负载均衡**：支持多节点负载分发

## 目录结构

```
gateway/
├── backend/         # 后端节点连接
│   ├── BackendChannelHandler.java   # 后端消息处理
│   └── BackendChannelManager.java   # 后端连接管理
├── config/          # 配置类
├── handler/         # 前端处理器
│   ├── FrontendChannelInitializer.java  # 初始化器
│   └── FrontendHandler.java             # 客户端消息处理
├── route/           # 路由
│   └── NodeRouter.java      # 节点路由器
├── session/         # 会话
│   ├── GatewaySession.java  # 会话对象
│   └── SessionManager.java  # 会话管理
├── GatewayServer.java           # 网关服务器
└── velaGatewayApplication.java # 启动类
```

## 架构

```
Client ──► Gateway ──► Game Node
              │
              └──► Game Node
```

