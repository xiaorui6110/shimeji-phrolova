# 快速开始

面向开发者的环境搭建、构建与运行指引。

## 仓库结构

```
shimeji-phrolova/
├── pom.xml                 # Maven 构建（JDK 21，含 shade / jpackage 配置）
├── src/main/java/          # 源码（包根 com.group_finity.mascot）
├── conf/                   # 运行时配置（XML、多语言、Schema 等）
├── img/                    # 图像集（每子目录一个角色）
├── xiaorui/                # 内部研究笔记（不随应用分发）
└── docs-site/              # 本文档站点（VitePress）
```

## 环境要求

| 工具 | 版本 | 说明 |
|---|---|---|
| JDK | 21+ | 构建时由 enforcer 插件强制校验；低于 21 会直接报错 |
| Maven | 3.8+ | 依赖管理（jitpack 仓库） |
| Git | 任意 | 获取源码 |

项目依赖：JNA（原生接口）、Rhino（表达式引擎）、FlatLaf（界面主题）、AbsoluteLayout（设置窗体布局）。

## 构建

```bash
mvn clean compile     # 仅编译
mvn clean package     # 编译 + 打 fat jar + 资源复制
```

`package` 阶段由 `maven-shade-plugin` 产出包含全部依赖的 `target/Shimeji-ee.jar`，并同步把 `conf/`、`img/` 复制到 `target/` 下——这两者在设计上保持在 JAR **外部**，便于用户直接编辑。

## 运行

### 方式一：Maven 直接运行

```bash
mvn -P run
```

`run` profile 会先打包再以以下 JVM 参数启动主类 `com.group_finity.mascot.Main`：

```
-Xmx512M -Xms128M -XX:ReservedCodeCacheSize=128M
-XX:+UseG1GC -XX:MaxGCPauseMillis=50 -XX:+UseStringDeduplication
```

### 方式二：运行 JAR

```bash
java -jar target/Shimeji-ee.jar
```

JAR 清单文件（manifest）已包含以下条目，因此无需在命令行重复追加：

| 条目 | 作用 |
|---|---|
| `Class-Path: . conf/ img/` | 外部资源目录可被直接访问 |
| `Add-Opens` | 开放 `java.base/java.lang`、`java.desktop/sun.awt`、`java.desktop/java.awt` 等模块，供透明窗口等原生渲染使用 |
| `Enable-Native-Access: ALL-UNNAMED` | JDK 21+ 原生访问授权（JNA） |

## 首次启动与验证

1. 程序启动后驻留系统托盘
2. 右键托盘图标 → 生成角色
3. 观察桌宠在屏幕上闲逛；尝试拖拽抛出、点击、右键菜单

若需在无原生窗口的环境（CI、远程会话、双屏测试）下调试，可将 `conf/settings.properties` 的 `Environment` 改为 `virtual`，吉祥物将显示在普通窗口内（见[配置系统](/development/configuration)）。

## 常见问题

| 现象 | 处理 |
|---|---|
| `This project requires Java 21 or higher` | 升级 JDK 或将 `JAVA_HOME` 指向 JDK 21+ |
| Linux 下无托盘图标 | Wayland 会话限制所致，可改为 X11 会话，或直接右键吉祥物操作 |
| 高 DPI 下角色偏小/发虚 | 在设置中调整缩放与滤镜（Bicubic/HQX） |
