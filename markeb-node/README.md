# markeb Node

游戏服务器节点，处理游戏业务逻辑。

## 功能特性

- **Actor 模型**：基于 Actor 的玩家状态管理
- **消息处理**：自动注册的 Handler 机制
- **事件系统**：支持事件发布订阅

## 目录结构

```
game/
├── actor/           # Actor 相关
│   ├── Player.java              # 玩家对象
│   ├── PlayerState.java         # 玩家状态
│   ├── PlayerActorBehavior.java # Actor 行为定义
│   └── PlayerActorService.java  # 玩家 Actor 服务
├── config/          # 配置类
├── event/           # 事件定义
│   ├── PlayerLogin.java     # 登录事件
│   ├── PlayerLogout.java    # 登出事件
│   └── handler/             # 事件处理器
├── handler/         # 消息处理器
│   └── TestHandler.java     # 测试 Handler
├── manager/         # 管理器
│   └── PlayerManager.java   # 玩家管理
├── netty/           # 网络层
│   ├── ServerHandler.java           # 传统处理器
│   ├── ActorServerHandler.java      # Actor 处理器
│   └── ServerChannelInitializerProvider.java
└── BootstrapApplication.java  # 启动类
```

## 配置

参见 `src/main/resources/application.yaml`

