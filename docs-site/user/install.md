# 安装指南

## 系统要求

- Windows 10/11 (64位)
- Java 21+ (如果使用JAR版本)

## 安装方式

### 方式一：便携版 (推荐)

1. 下载 `Shimeji-ee_x.x.x_Portable.zip`
2. 解压到任意目录
3. 运行 `Shimeji-ee.exe`

### 方式二：MSI安装包

1. 下载 `Shimeji-ee-x.x.x.msi`
2. 双击运行安装程序
3. 按照向导完成安装
4. 从开始菜单启动应用

### 方式三：JAR版本

1. 确保已安装 Java 21+
2. 下载 `Shimeji-ee.jar`
3. 在命令行中运行：

```bash
java --enable-native-access=ALL-UNNAMED \
     --add-opens=java.base/java.lang=ALL-UNNAMED \
     --add-opens=java.desktop/sun.awt=ALL-UNNAMED \
     --add-opens=java.desktop/java.awt=ALL-UNNAMED \
     -jar Shimeji-ee.jar
```

## 自定义配置

安装后，您可以修改以下文件来自定义您的Shimeji：

- `conf/actions.xml` - 动作配置
- `conf/behaviors.xml` - 行为配置  
- `conf/settings.properties` - 基本设置
- `img/` - 图像资源文件夹

## 常见问题

### 无法启动

- 确保已安装 Java 21+
- 检查是否有杀毒软件阻止运行

### 性能问题

- 减少同时运行的Shimeji数量
- 关闭不必要的动画效果

### 自定义角色

请参考 [教程文档](/user/tutorial/) 中的详细说明。

## 下载链接

前往 [GitHub Releases](https://github.com/DCRepairCenter/ShimejiLive/releases) 页面下载最新版本。
