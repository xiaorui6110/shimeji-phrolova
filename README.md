# Shimeji-Phrolova

> 桌面吉祥物应用程序 —— 让可爱的角色在您的屏幕上自由活动、攀爬与玩耍。

Shimeji-Phrolova 是一个基于 **Java 21** 的跨平台桌面吉祥物（Desktop Mascot）应用。你可以在桌面上生成一个个会四处游荡、攀爬、玩耍的角色，支持拖拽、抛出与点击交互；行为、动作与动画全部由外部 XML 与图像集描述，无需编写代码即可定制属于你自己的桌面伙伴。

支持 **Windows / macOS / Linux**。

## 特性

- **完全配置驱动**：行为、动作与动画由 `conf/` 下的 XML 描述，图像集即插即用，并附带 XSD 校验
- **原生透明窗口**：基于 JNA 调用系统原生能力，无边框透明角色，无需额外运行环境
- **多显示器支持**：角色可以在不同屏幕之间自由移动，并站立在窗口边缘或顶部
- **现代化性能**：基于 Java 21 构建并做 JVM 调优，可选 HQX 像素放大滤镜
- **多语言界面**：简体中文 / English / 日本語
- **多平台打包**：fat jar、Windows 便携版与 MSI 安装包、macOS DMG、Linux deb / rpm

## 关于核心角色 Phrolova

> **Phrolova** 是本项目的核心角色，项目名称即取自于她。目前她的形象图像集仍在绘制中，专属的动作与行为也尚未实现，相关素材将在后续版本中陆续补充上线。在此之前，你可以通过下方「配置与定制」一节，使用自定义图像集先行体验完整的角色定制能力。

## 目录结构

```
shimeji-phrolova/
├── src/               Java 源码（com.group_finity.mascot.*）
├── conf/              运行配置：行为/动作 XML、XSD、schema、多语言、主题与日志设置
├── img/               角色图像集（按动作分帧）
├── macos-resources/   macOS jpackage 打包资源
├── docs-site/         官方文档站点源码（VitePress）
├── pom.xml            Maven 构建配置（含运行与 jpackage 打包 profile）
└── LICENSE            许可证
```

## 快速开始

环境要求：**JDK 21+**、**Maven 3.9+**

```bash
git clone https://github.com/xiaorui6110/shimeji-phrolova.git
cd shimeji-phrolova

# 方式一：一键运行（conf/ 与 img/ 资源自动就绪）
mvn -P run

# 方式二：构建可执行 fat jar 后手动运行
mvn clean package
java -jar target/Shimeji-ee.jar
```

系统要求、首次使用与卸载步骤见[用户指南](docs-site/user/install.md)。

## 配置与定制

- 角色外观：替换或扩展 `img/` 下的图像集
- 角色行为：编辑 `conf/actions.xml`、`conf/behaviors.xml`（结构由 `conf/Mascot.xsd` 约束）
- 界面与语言：`conf/settings.properties`、`conf/language_*.properties`

更完整的机制说明（行为系统、渲染管线、平台抽象）参见[架构文档](docs-site/development/architecture.md)。

## 文档

完整文档站源码位于 [`docs-site/`](docs-site/)，本地预览：

```bash
cd docs-site
npm install
npm run dev      # 浏览器访问 http://localhost:5173
```

构建静态站点使用 `npm run build`，产物输出到 `docs-site/.vitepress/dist/`。

## 构建与打包

| Profile | 说明 |
| --- | --- |
| `mvn -P run` | 直接运行（开发调试） |
| `mvn clean package` | 生成 fat jar（`target/Shimeji-ee.jar`） |
| `mvn -P jpackage-win` | Windows 便携版 zip + MSI 安装包 |
| `mvn -P jpackage-mac` | macOS DMG（需在 macOS 上执行） |
| `mvn -P jpackage-linux` | Linux deb / rpm（需在 Linux 上执行） |

打包细节见[打包指南](docs-site/development/packaging.md)。

## 技术栈

Java 21 · Swing / AWT · FlatLaf · JNA · Maven（shade / exec / resources）· jpackage

## 许可

本项目代码以 [MIT 许可](LICENSE) 发布；其中沿用自上游开源实现（Shimeji、Shimeji-ee）的部分按其原始许可（zlib、BSD-2-Clause）使用。
