# 动作系统

Shimeji-Live 的动作系统是整个应用的核心，它定义了角色如何在屏幕上移动和表现。

## 动作类型

### 基础动作类型

1. **Stay** - 静止动作
   - 角色保持在当前位置
   - 通常用于待机状态

2. **Move** - 移动动作
   - 角色在屏幕上移动
   - 可以设置目标位置和速度

3. **Animate** - 动画动作
   - 播放指定的动画序列
   - 用于表情变化或特殊动作

4. **Embedded** - 嵌入式动作
   - 由 Java 代码实现的复杂动作
   - 用于实现高级功能如互动

## 动作配置

### XML 结构

```xml
<Action Name="Walk" Type="Move" Class="">
    <Animation Condition="" Loop="" ...>
        <Pose Image="walk1.png" ImageAnchor="Bottom" Velocity="2,0" Duration="1000"/>
        <Pose Image="walk2.png" ImageAnchor="Bottom" Velocity="2,0" Duration="1000"/>
    </Animation>
</Action>
```

### 主要属性

- `Name` - 动作名称（唯一标识符）
- `Type` - 动作类型
- `Class` - Java 类（仅 Embedded 类型需要）

## 动画系统

### Pose 元素

每个 `Pose` 定义动画的一帧：

- `Image` - 图像文件名
- `ImageAnchor` - 图像锚点
- `Velocity` - 移动速度
- `Duration` - 持续时间

### 条件系统

使用 `Condition` 属性来控制动作执行：

```xml
<Animation Condition="#{mascot.anchor.x}&lt;#{screen.width/2}">
    <!-- 当角色在屏幕左半部分时执行 -->
</Animation>
```

## 实际示例

### 基本走路动作

```xml
<Action Name="WalkLeft" Type="Move">
    <Animation Loop="true">
        <Pose Image="walk_left_1.png" ImageAnchor="Bottom" Velocity="-2,0" Duration="500"/>
        <Pose Image="walk_left_2.png" ImageAnchor="Bottom" Velocity="-2,0" Duration="500"/>
    </Animation>
</Action>
```

### 条件动作

```xml
<Action Name="ClimbWall" Type="Move" 
        Condition="#{mascot.environment.ceiling.containsPoint(mascot.anchor.x,mascot.anchor.y-1)}">
    <!-- 爬墙动作 -->
</Action>
```

## 高级话题

### 自定义动作

可以通过继承 `Action` 类来创建自定义动作：

```java
public class CustomAction extends Action {
    @Override
    public void apply(Mascot mascot) {
        // 自定义逻辑
    }
}
```

### 性能考虑

- 避免过于复杂的条件判断
- 合理设置动画帧率
- 优化图像资源大小

更多详细信息请参考源代码和示例配置。
