# 🤖 AGENTS.md

This document tracks the development history and architectural decisions made during the creation of the Vanta game server framework. It serves as a changelog for AI-assisted development sessions.

---

## 📅 2025-10-28 - Initial Framework Release

**Commit**: `dc8a0be` - "调整持久化池的初始时机"  
**Author**: CongCong Liao

### 🎯 Project Overview

Created a comprehensive game server framework from scratch with modular architecture. The framework provides production-ready capabilities for building high-performance multiplayer game servers.

### 🏗️ Core Architecture Components

#### **1. Network Layer (`network-spring-boot-starter`)**

A pluggable networking framework built on **Netty** and integrated with Spring Boot:

- **Auto-configuration support**: `NetworkAutoConfiguration` provides automatic bean wiring
- **Protocol Buffer integration**: `ProtoBuffGameDecoder` and `ProtoBuffGameEncoder` for efficient binary serialization
- **Message routing**: `MessageHandlerRegistrar` enables annotation-based message handling via `@MessageHandler`
- **Extensible design**: 
  - `ChannelInitializerProvider` for custom channel pipeline configuration
  - `BusinessHandlerProvider` for custom business logic injection
  - `IGameParser` and `IMessagePool` interfaces for protocol flexibility

**Key classes**:
- `NettyServer` - Core server implementation with lifecycle management
- `DefaultChannelInitializer` - Default Netty channel pipeline setup
- `GameActorContext` - Context management for connected players
- `ProtoBuffGameMessagePool` - Message pooling for ProtoBuf messages

#### **2. Persistence System (`persistent-*` modules)**

A three-tier persistence architecture supporting async data operations:

##### **persistent-core**
- **Dual-storage strategy**: Redis cache + MongoDB persistence
- **Batch processing**: `PersistentPool` manages batched writes to reduce database load
- **Health checks**: Connectivity checkers for Redis, MongoDB, and RocketMQ
  - `RedisConnectivityChecker`
  - `MongoConnectivityChecker`
  - `RocketMQProducerChecker` / `RocketMQConsumerChecker`
- **Interface design**:
  - `IPersistent` - Base interface for persistable entities
  - `IRolePersistent` - Role-specific persistence operations
  - `ICommonPersistent` - Common persistence operations
  - `IPersistentService` - Service layer interface

##### **persistent-producer**
- **Message queue integration**: `PersistentMessageProducer` sends persistence tasks to RocketMQ
- **Template pattern**: `PersistentTemplate` provides high-level API for data operations
- **Data center**: `DataCenter` manages in-memory data with persistence callbacks
- **Callback support**: `DefaultPersistentMqSendCallback` for async result handling

##### **persistent-consumer**
- **Ordered consumption**: `PersistentMessageConsumer` ensures ordered processing of persistence messages
- **Standalone deployment**: Can be deployed as separate service for scaling
- **Decoupled architecture**: Consumes from RocketMQ and writes to MongoDB independently

##### **persistent-entity**
- **Domain models**: 
  - `Role` - Player role entity
  - `Backpack` - Inventory entity

#### **3. Redis Event Bus (`redis-eventbus`)**

A distributed event system using Redis pub/sub:

- **Cross-service communication**: `RedisEventPublisher` and `RedisEventSubscriber` for event-driven architecture
- **Protostuff serialization**: Efficient binary serialization via `RedisEventProtostuffSchemaPool`
- **Event model**:
  - `RedisEvent` - Base event interface
  - `RedisEventWrapper` - Event envelope with metadata
  - `@RedisEventAction` - Annotation for event handlers
- **Topic-based routing**: Configurable via `RedisEventBusConfig`

#### **4. Service Registry (`service-registry`)**

Nacos integration for service discovery:

- **Non-web mode support**: `NacosNonWebAutoRegistrar` for TCP server registration
- **Auto-configuration**: `NonWebNacosConfig` handles Nacos client setup
- **Cloud-native**: Integrates with Alibaba Cloud ecosystem

#### **5. Common Utilities (`vanta-common`)**

Shared utilities across modules:

- **Codec support**: `AbsProtostuffSchemaPool` for custom Protostuff schema management
- **Event system**: `NetworkStartedEvent` for framework lifecycle events
- **Class scanning**: `ClassScanner` for annotation-based component discovery

#### **6. Protocol Definitions (`proto-message`)**

ProtoBuf message definitions:

- `options.proto` - Custom protobuf options
- `test.proto` - Test message definitions
- Generated Java classes for type-safe message handling

#### **7. Bootstrap Application**

Reference implementation demonstrating framework usage:

**Core components**:
- `BootstrapApplication` - Main entry point
- `Player` - Actor representing connected player
- `PlayerManager` - Player lifecycle management
- `ComponentConfig` - Spring bean configuration

**Networking**:
- `ServerHandler` - Business logic handler
- `ServerChannelInitializerProvider` - Custom channel pipeline
- `ChannelAttributeKey` - Channel attribute constants

**Event handling**:
- `EventHandler` - Framework event processor
- `PlayerLogin` / `PlayerLogout` - Login/logout event definitions
- `TestHandler` - Message handler example using `@MessageHandler`

**Tests**:
- `NettyClientSendReceiveTest` - Client-server communication test
- `PersistentTest` - Persistence system validation
- `RedisEventBusTest` - Event bus functionality test

### 🔧 Technology Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| **Java** | 21 | Programming language |
| **Spring Boot** | 3.5.5 | Application framework |
| **Netty** | 4.x | Network communication |
| **RocketMQ** | 2.3.0 | Message queue |
| **MongoDB** | 6.x | Database |
| **Redis** | 7.x | Cache & pub/sub |
| **ProtoBuf** | 4.30.2 | Binary serialization |
| **Nacos** | 2025.0.0.0 | Service discovery |
| **Lombok** | - | Code generation |

### 📦 Module Structure

```
vanta/
├── network-spring-boot-starter/    # Network layer (reusable)
├── persistent-core/                # Persistence framework (reusable)
├── persistent-producer/            # MQ producer (reusable)
├── persistent-consumer/            # MQ consumer (standalone service)
├── persistent-entity/              # Data models
├── redis-eventbus/                 # Event bus (reusable)
├── service-registry/               # Nacos integration (reusable)
├── vanta-common/                   # Common utilities
├── proto-message/                  # Protocol definitions
└── bootstrap/                      # Reference application
```

### 🎨 Design Patterns & Principles

1. **Spring Boot Starter Pattern**: All core modules use auto-configuration
2. **Factory Pattern**: `ChannelInitializerProvider`, `BusinessHandlerProvider`
3. **Template Pattern**: `PersistentTemplate`, `RedisTemplate`
4. **Strategy Pattern**: Pluggable `IGameParser` and `IMessagePool`
5. **Observer Pattern**: Event handlers with `@RedisEventAction`
6. **Builder Pattern**: Configuration through properties files
7. **Dependency Injection**: Extensive use of Spring IoC

### ⚙️ Configuration

**Application profiles**:
- `application.yaml` - Production configuration
- `application-local.yaml` - Local development configuration

**Run configurations** (`.run/`):
- `BootstrapApplication.run.xml` - Main game server
- `PersistentConsumerApplication.run.xml` - Consumer service

### 🔑 Key Features

✅ **Implemented**:
- Modular Spring Boot Starter architecture
- ProtoBuf-based network protocol
- Redis + MongoDB dual-storage persistence
- RocketMQ async persistence queue
- Redis pub/sub event bus
- Nacos service registration
- Health check system
- Message routing and handling
- Player session management
- Configurable via YAML

🔄 **In Progress**:
- Persistence pool initialization timing adjustment (this commit)

📋 **Planned**:
- Rate limiting at framework level
- Actor concurrency model
- Framework/example code separation
- Enhanced ordered consumption
- Idempotent message processing
- Docker support

### 📝 Important Implementation Notes

1. **Persistence Pool Timing**: This commit adjusts when the `PersistentPool` initializes to ensure proper dependency resolution with MongoDB and Redis connections.

2. **Message Handler Registration**: Uses Spring's component scanning with `@MessageHandler` annotation for automatic message routing.

3. **Event Bus Serialization**: Chose Protostuff over Java serialization for better performance and cross-language compatibility potential.

4. **Netty Pipeline**: Customizable via `ChannelInitializerProvider` interface, allowing projects to inject custom handlers.

5. **Data Consistency**: Redis serves as L1 cache with MongoDB as source of truth. Batch writes optimize database load.

---

## 🔮 Future Development Roadmap

### Phase 1: Core Enhancements
- [ ] Implement player-level rate limiting (Guava RateLimiter)
- [ ] Add message-type-specific rate limits
- [ ] Enhanced logging and monitoring hooks
- [ ] Metrics collection framework

### Phase 2: Concurrency Model
- [ ] Actor model integration
- [ ] Task queue per player
- [ ] Thread pool optimization
- [ ] Deadlock prevention mechanisms

### Phase 3: Production Readiness
- [ ] Graceful shutdown handling
- [ ] Circuit breaker patterns
- [ ] Retry strategies for MQ
- [ ] Data migration tools

### Phase 4: Developer Experience
- [ ] Separate framework from examples
- [ ] Comprehensive documentation
- [ ] Quick start templates
- [ ] Docker Compose setup

---

## 📚 Documentation Links

- Main documentation: [README.md](README.md)
- Protocol definitions: [proto-message/src/main/proto/](proto-message/src/main/proto/)
- Configuration examples: [bootstrap/src/main/resources/](bootstrap/src/main/resources/)

---

*This document is maintained to track architectural decisions and development progress. Each significant change should be documented here with context and rationale.*
