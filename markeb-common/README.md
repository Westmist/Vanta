# markeb Common

公共工具类和基础组件。

## 目录结构

```
common/
├── codec/           # 序列化相关
│   └── AbsProtostuffSchemaPool.java  # Protostuff Schema 池
├── cons/            # 常量定义
│   ├── Env.java             # 环境变量
│   └── ServerConstant.java  # 服务器常量
├── event/           # 公共事件
│   └── NetworkStartedEvent.java  # 网络启动事件
└── scanner/         # 扫描工具
    └── ClassScanner.java    # 类扫描器
```

## 主要功能

- **ClassScanner**：扫描指定包下的类，支持按注解/父类过滤
- **Protostuff Schema 池**：复用 Schema 提升序列化性能
- **公共事件**：框架内部事件定义

