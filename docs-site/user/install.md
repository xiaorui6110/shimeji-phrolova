# 安装与运行

## 系统要求

- **操作系统**：Windows 10/11、macOS、Linux（X11 / Wayland 均可运行）
- **运行时**：Java 21 或更高版本（直接运行 JAR 时需要；使用安装包则无需单独安装）

## 获取方式

### 方式一：安装包 / 便携版（推荐）

通过构建脚本可生成各平台的便携版与应用安装包，产物位于 `target/` 目录：

| 平台 | 便携版（解压即用） | 安装包 |
|---|---|---|
| Windows | `Shimeji-ee_<版本>_Windows_Portable.zip` | `.msi`（需 WiX 工具链） |
| macOS | `Shimeji-ee_<版本>_macOS_Portable.zip` | `.dmg` |
| Linux | `Shimeji-ee_<版本>_Linux_Portable.zip` | `.deb`（Debian/Ubuntu）、`.rpm`（Fedora/openSUSE） |

便携版解压后直接运行 `Shimeji-ee.exe`（Windows）/ `Shimeji-ee`（macOS/Linux）即可，安装包安装后可从开始菜单或应用程序目录启动。

::: tip 说明
各平台安装包需在对应操作系统上构建（见[打包与分发](/development/packaging)）。应用内部名称沿用 `Shimeji-ee` 这一既有命名。
:::

### 方式二：从源码构建运行

适用于想体验最新功能或参与开发的用户：

```bash
# 1. 克隆仓库
git clone https://github.com/xiaorui6110/fll-shimeji.git
cd shimeji-phrolova

# 2. 一键构建并运行（使用 JDK 21）
mvn -P run
```

或分步执行：

```bash
mvn clean package          # 构建可执行 fat jar
java -jar target/Shimeji-ee.jar
```

::: warning 需要 Java 21+
构建时 Maven 会强制校验 JDK 版本不低于 21（`maven-enforcer-plugin`）。JAR 的清单文件已内置必要的 JVM 参数（模块开放、原生访问授权），直接 `java -jar` 即可，无需手动追加。
:::

## 首次运行

1. 启动后，程序会驻留**系统托盘**（Windows/macOS/Linux 桌面环境）
2. 右键托盘图标可唤出菜单：
   - **生成角色**：在桌面上放出吉祥物
   - **角色选择**：切换当前启用的图像集（多选并存）
   - **设置**：语言、缩放、透明度、图像滤镜等
3. 若托盘不可用（少数 Linux Wayland 会话），右键点击任一已生成的吉祥物亦可弹出菜单

角色的默认行为：在屏幕上闲逛、攀爬、掉落、可被拖拽与抛出、可站立于窗口边缘或顶部。

## 目录说明

运行目录下有两个关键目录，均可自由增删：

```
conf/     # 配置文件（见 开发文档 → 配置系统）
img/      # 图像集：每个子目录代表一个角色
```

将新的图像集目录放入 `img/` 并重启程序，即可在"角色选择"中启用；`conf/settings.properties` 中的 `ActiveShimeji` 控制默认启用的角色。

## 卸载

- 便携版：删除解压目录即可
- 安装包：通过系统"添加/删除程序"卸载（Linux 使用 `dpkg -r` / `rpm -e`）
