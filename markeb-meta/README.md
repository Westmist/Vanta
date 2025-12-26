# markeb Meta

游戏配置表管理模块，集成 [Luban](https://www.datable.cn/docs/intro) 配置解决方案，支持配置表热更新。

## 功能特性

- 🚀 **Luban 集成** - 支持 Luban 生成的二进制配置数据
- 🔄 **热更新支持** - 运行时重新加载配置表，无需重启服务
- 📁 **多数据源** - 支持从 Classpath 或文件系统加载配置
- 🔍 **文件监视** - 自动监视配置文件变化，触发热更新
- 🎯 **Spring Boot 集成** - 自动配置，开箱即用

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>org.markeb</groupId>
    <artifactId>markeb-meta</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 配置属性

```yaml
markeb:
  meta:
    enabled: true
    # 配置文件路径，支持 classpath: 和 file: 前缀
    path: classpath:meta/
    # 或使用文件系统路径（支持热更新）
    # path: file:./config/meta/
    suffix: .bytes
    hot-reload:
      enabled: true          # 启用热更新（仅 file: 路径支持）
      debounce-ms: 500       # 防抖延迟
```

### 3. 使用 Luban 生成配置代码

使用 Luban 工具生成 Java 代码，配置表类需要继承框架提供的基类：

```java
// 配置 Bean 示例
public class ItemConfig extends AbstractBean {
    private int id;
    private String name;
    private int price;

    @Override
    public void deserialize(ByteBuf buf) {
        this.id = buf.readInt();
        this.name = buf.readString();
        this.price = buf.readInt();
    }

    // getters...
}

// 配置表示例
public class TbItem extends AbstractTable<Integer, ItemConfig> {

    public TbItem() {
        super("TbItem");
    }

    @Override
    protected ItemConfig createBean(ByteBuf buf) {
        return new ItemConfig(buf);
    }

    @Override
    protected Integer getKey(ItemConfig bean) {
        return bean.getId();
    }
}
```

### 4. 注册配置表

实现 `TableRegistrar` 接口注册所有配置表：

```java
@Component
public class GameTableRegistrar implements TableRegistrar {

    @Override
    public void register(TableRegistry registry) {
        registry.register(TbItem.class, TbItem::new);
        registry.register(TbSkill.class, TbSkill::new);
        registry.register(TbMonster.class, TbMonster::new);
        // 注册更多配置表...
    }
}
```

### 5. 使用配置表

```java
@Service
public class ItemService {

    @Autowired
    private MetaManager metaManager;

    public ItemConfig getItem(int id) {
        TbItem tbItem = metaManager.getTable(TbItem.class);
        return tbItem.get(id);
    }

    public List<ItemConfig> getAllItems() {
        TbItem tbItem = metaManager.getTable(TbItem.class);
        return StreamSupport.stream(tbItem.getAll().spliterator(), false)
                .collect(Collectors.toList());
    }
}
```

## 热更新

### 自动热更新

当配置 `markeb.meta.hot-reload.enabled=true` 且使用文件系统路径时，模块会自动监视配置文件变化并触发重载。

### 手动热更新

```java
@Autowired
private MetaManager metaManager;

// 重载所有配置表
metaManager.reloadAll();

// 重载指定配置表
metaManager.reload(TbItem.class, TbSkill.class);

// 根据表名重载
metaManager.reloadByName("TbItem", "TbSkill");
```

### 监听热更新事件

```java
@Component
public class MetaReloadListener {

    @EventListener
    public void onMetaReload(MetaReloadEvent event) {
        if (event.isFullReload()) {
            log.info("All tables reloaded");
        } else {
            log.info("Tables reloaded: {}", event.getReloadedTables());
        }

        // 检查特定表是否被重载
        if (event.isTableReloaded("TbItem")) {
            // 刷新物品缓存...
        }
    }
}
```

## 核心类说明

| 类名 | 说明 |
|------|------|
| `MetaManager` | 配置表管理器，负责加载、访问和热更新 |
| `ByteBuf` | Luban 二进制数据读取缓冲区 |
| `IBean` | 配置 Bean 接口 |
| `AbstractBean` | 配置 Bean 抽象基类 |
| `ITable` | 配置表接口 |
| `AbstractTable` | 配置表抽象基类 |
| `Tables` | 配置表容器 |
| `TableLoader` | 配置数据加载器接口 |
| `FileTableLoader` | 文件系统加载器 |
| `ClasspathTableLoader` | Classpath 加载器 |
| `FileWatcher` | 配置文件监视器 |
| `MetaReloadEvent` | 热更新事件 |

## 与 Luban 集成

### Luban 代码生成配置

在 Luban 的配置中，选择 Java 语言并使用 binary 导出格式：

```json
{
  "groups": [
    {
      "names": ["server"],
      "codeTarget": "java-bin",
      "dataTarget": "bin"
    }
  ]
}
```

### 自定义代码模板

如果需要让 Luban 生成的代码直接继承本模块的基类，可以自定义 Luban 的代码模板。

## 最佳实践

1. **开发环境** - 使用 `file:` 路径并启用热更新，方便调试
2. **生产环境** - 使用 `classpath:` 路径，将配置打包到 JAR 中
3. **配置分离** - 将策划配置与程序配置分开管理
4. **版本控制** - 配置表数据应纳入版本控制

## 目录结构

```
markeb-meta/
├── luban/                              # Luban 工具目录（不纳入版本控制）
│   ├── Luban.dll / Luban.jar           # Luban 工具
│   └── Templates/                      # 自定义模板（可选）
├── src/main/java/org/markeb/meta/
│   ├── config/
│   │   ├── MetaAutoConfiguration.java  # Spring Boot 自动配置
│   │   └── MetaProperties.java         # 配置属性
│   ├── loader/
│   │   ├── ClasspathTableLoader.java   # Classpath 加载器
│   │   └── FileTableLoader.java        # 文件系统加载器
│   ├── luban/
│   │   ├── AbstractBean.java           # Bean 抽象基类
│   │   ├── AbstractTable.java          # Table 抽象基类
│   │   ├── ByteBuf.java                # 二进制缓冲区
│   │   ├── IBean.java                  # Bean 接口
│   │   ├── ITable.java                 # Table 接口
│   │   └── Tables.java                 # 表容器
│   ├── watcher/
│   │   └── FileWatcher.java            # 文件监视器
│   ├── gen/                            # ⭐ Luban 生成的配置表代码
│   │   ├── beans/                      # 配置 Bean 类
│   │   │   ├── ItemConfig.java
│   │   │   ├── SkillConfig.java
│   │   │   └── ...
│   │   ├── tables/                     # 配置表类
│   │   │   ├── TbItem.java
│   │   │   ├── TbSkill.java
│   │   │   └── ...
│   │   └── GameTableRegistrar.java     # 表注册器
│   ├── MetaManager.java                # 配置表管理器
│   ├── MetaReloadEvent.java            # 热更新事件
│   ├── TableLoader.java                # 加载器接口
│   ├── TableRegistrar.java             # 注册器接口
│   └── TableRegistry.java              # 注册表接口
└── src/main/resources/
    ├── meta/                           # ⭐ Luban 生成的二进制数据
    │   ├── TbItem.bytes
    │   ├── TbSkill.bytes
    │   └── ...
    └── META-INF/spring/
        └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

## Luban 生成代码存放位置

| 文件类型 | 存放位置 | 说明 |
|---------|---------|------|
| 配置 Bean 类 | `src/main/java/.../meta/gen/beans/` | Luban 生成的数据结构类 |
| 配置表类 | `src/main/java/.../meta/gen/tables/` | Luban 生成的表类 |
| 二进制数据 | `src/main/resources/meta/` | Luban 导出的 .bytes 文件 |
| Excel 源文件 | 项目外部（策划目录） | 策划维护的 Excel 配置表 |

