# markeb Mesh

游戏匹配服务，提供玩家匹配功能。

## 功能特性

- **匹配队列**：玩家排队匹配
- **匹配算法**：支持多种匹配策略
- **房间分配**：匹配成功后分配房间

## 目录结构

```
mesh/
├── match/           # 匹配相关
│   ├── MatchRequest.java    # 匹配请求
│   ├── MatchResult.java     # 匹配结果
│   └── MatchService.java    # 匹配服务
└── velaMeshApplication.java  # 启动类
```

## 使用示例

```java
@Autowired
private MatchService matchService;

// 加入匹配
MatchRequest request = new MatchRequest(playerId, gameMode);
matchService.joinMatch(request);

// 取消匹配
matchService.cancelMatch(playerId);
```

