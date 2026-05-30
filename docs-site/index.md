---
layout: home

hero:
  name: "Shimeji-Live"
  text: "桌面吉祥物应用程序"
  tagline: "让可爱的角色在您的屏幕上自由活动"
  actions:
    - theme: brand
      text: 快速开始
      link: /user/install
    - theme: alt
      text: 查看教程
      link: /user/tutorial/
    - theme: alt
      text: GitHub
      link: https://github.com/DCRepairCenter/ShimejiLive

features:
  - icon: 🎮
    title: 可爱的桌面角色
    details: 让可爱的角色在您的屏幕上自由活动，与您的桌面环境互动
  - icon: 🎨
    title: 自定义角色和动画
    details: 支持自定义角色、动画和行为，创造属于您自己的桌面伙伴
  - icon: 🖥️
    title: 多显示器支持
    details: 完美支持多显示器环境，角色可以在不同屏幕间自由移动
  - icon: ⚡
    title: 现代化性能
    details: 基于 Java 21，经过性能优化，提供流畅的用户体验
  - icon: 🎯
    title: 现代化界面
    details: 采用现代化的用户界面设计，操作简单直观
  - icon: 📦
    title: 多种安装方式
    details: 提供便携版、MSI 安装包和 JAR 版本，满足不同用户需求
---
## 🚀 快速开始

### 下载安装

前往 [Releases](https://github.com/BegoniaHe/dc-ShimejiLive/releases) 页面下载最新版本：

::: code-group

```bash
# 下载 Shimeji-ee_x.x.x_Portable.zip
# 解压后直接运行 Shimeji-ee.exe
```

```bash
# 下载 Shimeji-ee-x.x.x.msi
# 双击安装，从开始菜单启动
```

```bash
# 需要 Java 21+ 环境
java --enable-native-access=ALL-UNNAMED \
     --add-opens=java.base/java.lang=ALL-UNNAMED \
     --add-opens=java.desktop/sun.awt=ALL-UNNAMED \
     --add-opens=java.desktop/java.awt=ALL-UNNAMED \
     -jar Shimeji-ee.jar
```

:::

### 自定义配置

安装后，您可以修改以下文件来自定义您的 Shimeji：

- `conf/actions.xml` - 动作配置
- `conf/behaviors.xml` - 行为配置
- `conf/settings.properties` - 基本设置
- `img/` - 图像资源文件夹

## 📚 文档导航

<div class="vp-doc">
  <div class="custom-block tip">
    <p class="custom-block-title">💡 新手指南</p>
    <p>如果您是第一次使用 Shimeji，建议从 <a href="/user/install">安装指南</a> 开始，然后阅读 <a href="/user/tutorial/">教程系列</a>。</p>
  </div>
</div>

### 用户文档

- [📥 安装指南](/user/install) - 详细的安装和配置说明
- [📖 教程系列](/user/tutorial/) - 从入门到高级的完整教程

### 开发文档

- [⚙️ 开发指南](/development/getting-started) - 开发环境搭建和代码结构
- [💡 开发技巧](/development/tips) - 实用的开发技巧和最佳实践

## 🤝 贡献

欢迎贡献代码、报告问题或提出建议！

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用多重许可证：

- 原始 Shimeji 代码：[zlib License](https://www.zlib.net/zlib_license.html)
- Live 版本扩展：[MIT License](https://opensource.org/licenses/MIT)
