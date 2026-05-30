# 开发指南

欢迎来到 Shimeji-Live 开发文档！本指南将帮助您了解项目结构、设置开发环境，并开始贡献代码。

## 项目概述

Shimeji-Live 是一个基于 Java 的桌面吉祥物应用程序，让可爱的角色在用户屏幕上自由活动。项目使用现代化的 Java 技术栈，支持跨平台运行。

### 技术栈

- **Java 21+** - 主要编程语言
- **Maven** - 项目构建工具
- **Swing** - GUI 框架
- **JNA** - 原生系统调用
- **FlatLaf** - 现代化外观主题

## 快速开始

### 环境要求

- Java 21 或更高版本
- Maven 3.8+
- Git
- IDE（推荐 IntelliJ IDEA 或 Eclipse）

### 克隆和构建

```bash
# 克隆仓库
git clone https://github.com/DCRepairCenter/ShimejiLive.git
cd ShimejiLive

# 编译项目
mvn clean compile

# 运行应用
mvn -P run

# 打包
mvn clean package
```

### 开发模式运行

```bash
# 使用 Maven 直接运行
mvn exec:java -Dexec.mainClass="com.group_finity.mascot.Main"

# 或使用预定义的 Profile
mvn -P run
```

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   └── com/group_finity/mascot/
│   │       ├── Main.java              # 主入口
│   │       ├── Manager.java           # 吉祥物管理器
│   │       ├── Mascot.java            # 吉祥物类
│   │       ├── action/                # 动作实现
│   │       ├── behavior/              # 行为逻辑
│   │       ├── config/                # 配置解析
│   │       ├── environment/           # 环境检测
│   │       ├── image/                 # 图像处理
│   │       └── win/                   # Windows 特定实现
│   └── resources/                     # 资源文件
conf/                                  # 配置文件
img/                                   # 图像资源
docs/                                  # 文档
target/                                # 构建输出
```

## 核心概念

### 1. Mascot（吉祥物）

`Mascot` 类是核心实体，代表屏幕上的一个角色实例。每个吉祥物有自己的：
- 位置和速度
- 当前行为状态
- 显示窗口
- 动画序列

### 2. Behavior（行为）

行为定义了吉祥物的动作模式，通过 XML 配置文件定义：
- `behaviors.xml` - 行为列表和触发条件
- `actions.xml` - 具体动作实现

### 3. Environment（环境）

环境系统检测桌面状态：
- 屏幕边界
- 活动窗口
- 鼠标位置

## 调试技巧

### 启用调试模式

在 `conf/settings.properties` 中设置：
```properties
DebugMode=true
ShowDebugWindow=true
```

### 查看日志

日志文件位于应用程序目录下：
```bash
tail -f shimeji.log
```

### 性能分析

使用 JProfiler 或类似工具：
```bash
java -javaagent:jprofiler.jar -jar Shimeji-ee.jar
```

## 贡献指南

### 代码规范

- 使用 4 空格缩进
- 类名使用 PascalCase
- 方法名使用 camelCase
- 常量使用 UPPER_SNAKE_CASE

### 提交规范

```bash
# 功能添加
git commit -m "feat: 添加新的动作类型"

# 修复 bug
git commit -m "fix: 修复在多显示器环境下的位置计算问题"

# 文档更新
git commit -m "docs: 更新开发指南"
```

### Pull Request 流程

1. Fork 项目到你的账户
2. 创建功能分支：`git checkout -b feature/new-feature`
3. 提交更改：`git commit -am 'Add new feature'`
4. 推送分支：`git push origin feature/new-feature`
5. 创建 Pull Request

## 常见问题

### Q: 如何添加新的动作？

A: 参考 [动作系统文档](/development/actions) 了解详细步骤。

### Q: 如何支持新的操作系统？

A: 需要实现 `NativeFactory` 接口，参考现有的 Windows 实现。

### Q: 如何调试配置文件解析问题？

A: 启用调试模式，查看控制台输出的 XML 解析日志。

## 相关文档

- [动作系统详解](/development/actions)
- [配置文件格式](/development/configuration)
- [构建和部署](/development/build-deploy)

---

如有问题，欢迎在 [GitHub Issues](https://github.com/DCRepairCenter/ShimejiLive/issues) 中提出。
