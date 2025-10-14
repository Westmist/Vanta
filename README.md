# 🎮 Game Server Framework

一个从 **0 到 1** 独立开发的 **高性能游戏服务端框架**。  
框架基于 **Spring Boot + Netty + RocketMQ + Redis + MongoDB** 构建，支持模块化扩展、异步持久化、消息限流与可插拔网络层。

---

## 🧱 项目结构
vanta  
├── .run/ # IntelliJ IDEA 运行配置目录  
├── bootstrap # 启动模块（示例服务端或集成入口）  
├── network-spring-boot-starter # 网络层通用启动器（Netty + Spring Boot）  
├── persistent-consumer # RocketMQ 消费者模块（有序消费实现）  
├── persistent-core # 持久化核心模块（Redis + MongoDB 支撑）  
├── persistent-entity # 持久化实体定义模块（业务方定义）  
├── persistent-producer # RocketMQ 生产者模块（消息发送封装）  
└── proto-message # 协议定义模块（.proto 文件生成的代码）

---

## ⚙️ 可封装成依赖的核心模块

| 模块名                             | 说明                                                            |
|:--------------------------------|:--------------------------------------------------------------|
| **network-spring-boot-starter** | 提供可插拔的网络层框架，基于 Netty 封装连接、消息编解码、消息分发、Handler 注册等。后续将默认集成限流功能。 |
| **persistent-core**             | 持久化基础能力层，统一管理数据缓存（Redis）与数据库（MongoDB）间的同步、批量落盘、异步更新等逻辑。       |
| **persistent-producer**         | 消息生产端封装，支持异步可靠投递到 RocketMQ，用于跨模块或异步任务场景。                      |

> 以上模块可被外部游戏服务直接作为依赖引入，实现业务逻辑快速集成。

---

## 🚀 功能特性

- **模块化架构**：可插拔式组件设计，清晰的职责分层。
- **高性能网络层**：基于 Netty 封装，支持消息池与消息路由机制。
- **异步持久化**：Redis 缓存与 MongoDB 数据库的协同更新，支持批量异步写入。
- **消息队列支持**：RocketMQ 消费生产分离，确保一致性与扩展性。
- **ProtoBuf 通信协议**：客户端与服务端通过统一的 `.proto` 定义生成代码，减少耦合。
- **Spring Boot Starter**：内置 Starter 模式，可快速集成到任意 Spring Boot 项目。
- **限流机制（规划中）**：框架层默认实现基于玩家 ID 的限流，支持不同消息类型自定义速率。
- **Actor 并发模型（规划中）**：将引入 Actor 模型封装框架层的并发逻辑，保证任务串行与安全。

---

## 🧩 模块说明

### `network-spring-boot-starter`

- 封装 Netty 初始化、连接管理与消息分发。
- 提供可扩展的消息处理机制。
- 支持配置化加载、自动装配。
- **计划中**：增加框架层默认限流功能（基于 Guava RateLimiter 或自定义实现）。

### `persistent-core`

- 管理 Redis 缓存与 MongoDB 数据存储。
- 提供通用 `DataManager` 接口，支持异步批量持久化。
- 支持 IPersistent 对象统一保存。
- 提供持久化生命周期管理机制。

### `persistent-producer`

- 封装 RocketMQ 消息生产逻辑。
- 提供统一的消息发送接口。
- 支持异步发送与回调机制。

### `persistent-consumer`

- 负责 RocketMQ 消费端逻辑。
- 计划支持 **有序消费**、**幂等处理** 与 **分区路由**。

### `persistent-entity`

- 定义业务层的数据实体。
- 与 `persistent-core` 模块协作实现数据的自动持久化。

### `proto-message`

- 统一存放 `.proto` 文件与生成的 Java 类。
- 保证客户端与服务端通信协议的一致性。

### `bootstrap`

- 游戏服务端启动入口。
- 集成所有核心模块。
- 用于框架功能的示例验证与测试。
- 后续将拆分为：
    - **框架部分**：纯框架核心；
    - **示例部分**：演示功能及测试样例。

---

## 🔧 环境要求

| 组件          | 版本   |
|:------------|:-----|
| JDK         | 21   |
| Maven       | 3.8+ |
| Spring Boot | 3.x  |
| RocketMQ    | 5.x  |
| MongoDB     | 6.x  |
| Redis       | 7.x  |

---

## 🏃 构建与启动

### 方式一：推荐使用 **IntelliJ IDEA** 启动

在项目根目录下的 `.run/` 文件夹中提供了预定义的运行配置：

| 配置文件                                              | 启动目标   | 说明                |
|:--------------------------------------------------|:-------|:------------------|
| `BootstrapApplication.run.xml`                    | 游戏服务端  | 启动核心服务端逻辑         |
| `PersistentConsumerApplication.run.xml`           | 持久化消费者 | 启动 RocketMQ 消费者服务 |
| `NettyClientSendReceiveTest.contextLoads.run.xml` | 客户端测试  | 启动客户端连接并测试消息收发    |

**步骤：**

1. 打开项目于 IntelliJ IDEA；
2. 确保 `.run/` 目录存在；
3. IDEA 会自动识别并加载这些运行配置；
4. 选择对应的配置 → 点击运行按钮 ▶️。

> ⚠️ 启动前请确保 RocketMQ、Redis、MongoDB 已正确运行并在配置文件中设置好连接地址。

---

### 方式二：命令行构建启动

```bash
# 清理并构建项目
mvn clean package -DskipTests

# 启动服务端（bootstrap 模块）
java -jar bootstrap/target/bootstrap.jar
```

🧭 后续规划  
功能 说明  
✅ 网络层封装 已实现 Spring Boot Starter 模式  
🔄 持久化完善 增强有序消费、消息重试机制  
🧠 并发模型 封装 Actor 模型，统一任务调度与线程隔离  
⚙️ 限流机制 在网络层增加默认限流功能（玩家级别、消息级别）  
🧩 框架拆分 将示例部分独立于框架模块外，形成纯依赖包结构  
🐳 Docker 支持 提供一键运行的容器化部署方案

🧑‍💻 作者说明

本项目由个人从零构建，旨在探索一套可用于商业化游戏项目的服务端框架。
欢迎大家提出建议与改进意见！