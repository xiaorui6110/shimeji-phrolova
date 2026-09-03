# 打包与分发

本项目基于 JDK 自带的 `jpackage` 生成各平台安装包，配合 Maven profile 实现“一条命令出包”。所有产物命名沿用既有应用名 `Shimeji-ee`。

## 产物总览

| 目标 | 命令 | 产物 |
|---|---|---|
| fat jar | `mvn clean package` | `target/Shimeji-ee.jar`（含全部依赖） |
| Windows 便携版 | `mvn clean install -P jpackage-win` | `Shimeji-ee_<版本>_Windows_Portable.zip` |
| Windows 安装包 | 同上 | `*.msi` |
| macOS 便携版 | `mvn clean install -P jpackage-mac` | `Shimeji-ee_<版本>_macOS_Portable.zip`（.app） |
| macOS 安装包 | 同上 | `*.dmg` |
| Linux 便携版 | `mvn clean install -P jpackage-linux` | `Shimeji-ee_<版本>_Linux_Portable.zip` |
| Linux 安装包 | 同上 | `*.deb`、`*.rpm` |

## 构建前置条件

- **JDK 21+**：`jpackage` 工具随 JDK 分发，需保证 `jpackage` 在 `PATH` 中
- **平台绑定**：`jpackage` 只能在目标平台上构建对应格式——
  - Windows MSI 需额外安装 [WiX Toolset](https://wixtoolset.org/)（生成 `.msi` 的依赖）
  - macOS / Linux 包需在 macOS / Linux 主机上执行
- 图标：Windows 使用 `img/icon.ico`，macOS/Linux 使用 `img/icon.png`（pom 中已引用）

## 执行示例

```bash
# Windows（便携版 zip + MSI）
mvn clean install -P jpackage-win

# macOS（.app zip + DMG）
mvn clean install -P jpackage-mac

# Linux（便携 zip + deb + rpm）
mvn clean install -P jpackage-linux
```

构建产物输出到 `target/` 根目录。`jpackage` 的中间产物（app-image 等）保留在 `target/jpackage-*` 子目录，便于排查。

## JAR 内容策略

`maven-jar-plugin` 的资源配置刻意保持 JAR“小而纯”：

- **打进 JAR**：编译产物、`logging.properties`、`language*.properties`、`schema*.properties`（仅内置默认）
- **留在 JAR 外**：`conf/` 与 `img/`（构建时复制到 `target/` 旁，与 JAR 同级）

这样用户可直接编辑运行目录下的配置与图像，无需重新打包。

## 运行时 JVM 参数

JAR 清单已内置下列参数，分发后开箱即用：

```
# 内存与 GC（50ms 暂停目标、字符串去重）
-Xmx512M -Xms128M -XX:ReservedCodeCacheSize=128M
-XX:+UseG1GC -XX:MaxGCPauseMillis=50 -XX:+UseStringDeduplication

# JDK 模块开放（透明窗口/原生渲染所需）
--add-opens=java.base/java.lang=ALL-UNNAMED
--add-opens=java.desktop/sun.awt=ALL-UNNAMED
--add-opens=java.desktop/java.awt=ALL-UNNAMED
--enable-native-access=ALL-UNNAMED
```

`jpackage` 产物通过 `--java-options` 逐项传入同一组参数（macOS/Linux 额外含 `-Djava.awt.headless=false`、`-Duser.dir=$APPDIR` 等平台项）。

## 验证清单

发布前建议按序确认：

1. `java -jar target/Shimeji-ee.jar` 可运行（托盘 → 生成角色）
2. 便携版解压后目录含 `Shimeji-ee.jar`、`conf/`、`img/`，直接启动正常
3. 安装包安装后可启动、可卸载
4. 运行日志无 `XML schema issues` 之外的异常（Schema 校验为仅告警模式）
