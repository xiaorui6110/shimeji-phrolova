# 配置系统

运行时配置位于程序目录的 `conf/` 下，均采用可编辑的文本格式，修改后重启生效。

## 配置文件一览

| 文件 | 作用 |
|---|---|
| `settings.properties` | 用户主配置（语言、缩放、透明度、启用的角色等） |
| `actions.xml` | 动作定义：动作类型与逐帧姿态（Pose） |
| `behaviors.xml` | 行为定义：行为引用、频率、条件与分组 |
| `Mascot.xsd` | 上述 XML 的 Schema；启动加载时校验，仅记录告警不阻断 |
| `language*.properties` | 界面多语言（`zh` / `en` / `ja` + `language.properties` 回退） |
| `schema*.properties` | XML 标签名的语言映射（日文/英文两套方言标签的间接引用层） |
| `logging.properties` | Java 日志级别与输出 |
| `theme.properties` | 设置窗体的 FlatLaf 主题外观 |

### 查找优先级

同一文件存在多份时按以下顺序覆盖（后面的优先）：

```
conf/                  ← 全局默认
img/<图像集名>/conf/    ← 该图像集专属配置（可覆盖全局）
```

每个图像集目录可自带 `actions.xml`、`behaviors.xml` 等，实现“一图集一玩法”。

## settings.properties 键

| 键 | 示例 | 说明 |
|---|---|---|
| `ActiveShimeji` | `imageSetA/imageSetB` | 默认启用的图像集，`/` 分隔多个 |
| `Language` | `zh-CN` | 界面语言（zh-CN / en-GB 等） |
| `Environment` | `generic` | 平台后端：`generic`(自动) / `virtual`(窗口内) / `wayland` |
| `Scaling` | `1.2` | 角色缩放系数 |
| `Opacity` | `1.0` | 角色整体透明度（0~1） |
| `Filter` | `bicubic` | 图像滤镜：`nearest` / `hqx` / `bicubic` |
| `Breeding` | `true` | 允许繁殖（分裂出新角色） |
| `AlwaysShowShimejiChooser` | `false` | 启动即显示角色选择器 |
| `AlwaysShowInformationScreen` | `false` | 启动即显示信息窗体 |
| `InteractiveWindows` | `Chat/Notepad/...` | 可交互窗口名单（角色可攀上这些窗口） |
| `InteractiveWindowsBlacklist` | — | 上述名单的排除项 |
| `WindowSize` | `600x500` | 设置窗体尺寸 |
| `MenuDPI` | `192` | 托盘菜单 DPI |
| `Background` / `BackgroundMode` / `BackgroundImage` | — | 信息/设置窗体背景 |

## actions.xml

定义**动作**（短时行为原语），结构示意：

```xml
<Actions>
  <Action Name="..." Type="Move">
    <Pose Image="..." ImageAnchor="bottomCenter" Velocity="2,-2" Duration="20"/>
    <Pose Image="..." ImageAnchor="bottomCenter" Velocity="2,-1" Duration="20"/>
  </Action>
</Actions>
```

- **`Action` 属性**：`Name`（唯一）、`Type`（动作类型）、`Duration`/`Condition`/`Draggable`（默认 true）/`Affordance`（交互标签）
- **Pose（帧）属性**：
  | 属性 | 说明 |
  |---|---|
  | `Image` / `ImageRight` | 左右朝向帧图（缺省右图时自动水平翻转） |
  | `ImageAnchor` | 图中“脚”的位置（锚点），用于贴地与定位 |
  | `Velocity` | 每帧位移 `dx,dy` |
  | `Duration` | 该帧持续的节拍数 |
  | `Sound` | 播放的音效 |
- **动作类型（内建）**：`Stay`、`Move`、`Animate`、`Jump`、`Breed`（分裂）、`ThrowIE`（抛出窗体）、`Interact`（与窗口交互）、`Sequence`（串行动作）、`Select`、`Broadcast`、`Regist`、`Dragged`（被抓取）
- **`Type="Embedded"`**：用 `Class` 属性直接引用 Java 动作类，供扩展动作类型
- 动作级还有 `BornMascot`（出生子角色）、`TransformMascot` / `TransformBehavior`（变身）

## behaviors.xml

定义**行为**（长期状态机）与切换规则：

```xml
<Behaviors>
  <Behavior Name="SitDown" Hidden="true">
    <NextBehaviorList>
      <BehaviorReference Name="Idle" Frequency="1"/>
    </NextBehaviorList>
  </Behavior>
</Behaviors>
```

- `BehaviorReference`：候选行为，`Name` + `Frequency`（权重）；`Frequency=0` 仅可被显式引用，不参与普通随机
- `Hidden`：不出现在右键菜单（基础行为通常隐藏）
- `<Condition>` 分组：内含多个 `BehaviorReference`，按条件动态生效
- `<NextBehaviorList>`：本行为结束时的候选集合；前一个动作若声明了它，会**追加**到全局候选（`Add="true"`）或**仅用**这些候选（`Add="false"`）

::: tip 必须存在的基础行为
`Fall`（坠落）、`Dragged`（被抓）、`Thrown`（被抛出）由代码硬编码引用，任何图像集都必须声明。
:::

## 变量与表达式

XML 属性值支持 Rhino 表达式（见[架构总览 → 表达式系统](/development/architecture#表达式系统)），常用上下文：

| 对象 | 可用成员（节选） |
|---|---|
| `mascot` | `anchor`、`imageSet`、`lookRight`、`count`、`totalCount`、`getBounds()` |
| `mascot.environment` | `screen`、`workArea`、`activeIE`、`cursor`、`floor`、`ceiling`、`leftWall`、`rightWall`、`isScreenTopBottom(...)` |
| `action` | 当前动作的注入变量 |
| `<Constant>` | 图像集常量（优先级低于 `mascot` 成员） |

## XML Schema 校验

`conf/Mascot.xsd` 描述 `actions.xml` / `behaviors.xml` 的合法结构（动作类型、姿态属性、行为引用关系、数值格式等）。程序启动加载 XML 时会对照该 Schema 做一次**仅告警**校验：

- 合法：静默通过
- 不合规：在日志中记录 WARNING（含行号与原因），**不影响加载与运行**

这样既可提示配置书写错误，又不会让一次笔误导致整个图像集无法使用。

## 多语言

- 界面文案存放于 `language_<locale>.properties`；缺失条目回退到 `language.properties`
- XML 标签名通过 `schema*.properties` 间接引用，同一份 `actions.xml`/`behaviors.xml` 可同时兼容英文与日文两种标签拼写

## 新增一个图像集

1. 在 `img/` 下新建目录（目录名即图像集名），放入动画帧 PNG
2. 在该目录下建 `conf/actions.xml`、`conf/behaviors.xml`（可复制其它图像集作为起点再修改）
3. 可选：`conf/info.xml` 描述角色信息；`conf/settings.properties` 覆盖全局设置
4. 重启程序，在“角色选择”中启用
