# 配置系统

Shimeji-Live 的配置系统允许您自定义角色的行为、外观和其他设置。

## 配置文件结构

### 主要配置文件

- `conf/actions.xml` - 动作定义
- `conf/behaviors.xml` - 行为模式
- `conf/settings.properties` - 基本设置
- `conf/language.properties` - 语言配置

### 图像资源

- `img/` - 角色图像文件夹
- 支持 PNG 格式的透明图像

## 基本设置

### settings.properties

```properties
# 动画间隔（毫秒）
AnimationDuration=40

# 同时显示的角色数量
MaxMascots=5

# 是否启用音效
SoundEffects=true

# DPI 设置
MenuDPI=96
```

### 常用设置

- `AnimationDuration` - 控制动画流畅度
- `MaxMascots` - 限制性能消耗
- `SoundEffects` - 启用/禁用音效
- `MenuDPI` - 界面缩放设置

## 行为配置

### behaviors.xml 结构

```xml
<BehaviorList>
    <Behavior Name="Normal" Frequency="10">
        <NextBehaviorList>
            <BehaviorReference Behavior="Walk" Frequency="5"/>
            <BehaviorReference Behavior="Sit" Frequency="3"/>
        </NextBehaviorList>
    </Behavior>
</BehaviorList>
```

### 行为属性

- `Name` - 行为名称
- `Frequency` - 执行频率
- `Hidden` - 是否在菜单中隐藏

## 动作配置

### actions.xml 基础

```xml
<ActionList>
    <Action Name="Stand" Type="Stay">
        <Animation>
            <Pose Image="stand.png" ImageAnchor="Bottom" Duration="1000"/>
        </Animation>
    </Action>
</ActionList>
```

### 图像锚点

- `Bottom` - 底部对齐（常用于地面站立）
- `Center` - 居中对齐
- `Top` - 顶部对齐

## 语言配置

### 多语言支持

创建对应的语言文件：

- `language_en.properties` - 英文
- `language_zh.properties` - 中文
- `language.properties` - 默认语言

### 配置示例

```properties
CallShimeji=召唤Shimeji
DismissAll=关闭程序
Settings=设置
```

## 高级配置

### 条件表达式

使用 JavaScript 语法的条件表达式：

```xml
Condition="#{mascot.anchor.x} &lt; #{screen.width/2}"
```

### 环境变量

- `#{screen.width}` - 屏幕宽度
- `#{screen.height}` - 屏幕高度
- `#{mascot.anchor.x}` - 角色 X 坐标
- `#{mascot.anchor.y}` - 角色 Y 坐标

## 调试配置

### 启用调试模式

在 `settings.properties` 中添加：

```properties
DebugMode=true
LogLevel=DEBUG
```

### 日志配置

修改 `logging.properties`：

```properties
# 设置日志级别
.level=INFO

# 文件输出
java.util.logging.FileHandler.pattern=shimeji.log
java.util.logging.FileHandler.formatter=com.group_finity.mascot.LogFormatter
```

## 配置验证

### XML 验证

所有 XML 文件都有对应的 XSD 架构文件进行验证：

- `conf/Mascot.xsd` - 主架构文件

### 常见错误

1. XML 格式错误
2. 图像文件路径错误
3. 条件表达式语法错误
4. 属性值超出范围

## 备份与恢复

### 配置备份

定期备份配置文件：

```bash
cp -r conf/ conf_backup/
```

### 恢复默认配置

删除修改的配置文件，重启应用会自动恢复默认配置。

更多配置选项请参考源代码中的示例和文档。
