# 构建与部署

本指南介绍如何构建和部署 Shimeji-Live 项目。

## 开发环境要求

### 必需软件

- **Java 21+** - OpenJDK 或 Oracle JDK
- **Maven 3.6+** - 构建工具
- **Git** - 版本控制

### 可选工具

- **IntelliJ IDEA** 或 **Eclipse** - IDE
- **Docker** - 容器化部署

## 构建项目

### 克隆项目

```bash
git clone https://github.com/DCRepairCenter/ShimejiLive.git
cd ShimejiLive
```

### Maven 构建

```bash
# 编译项目
mvn compile

# 运行测试
mvn test

# 打包 JAR
mvn package

# 清理构建产物
mvn clean
```

### 运行项目

```bash
# 直接运行
mvn -P run

# 或使用 JAR 文件
java --enable-native-access=ALL-UNNAMED \
     --add-opens=java.base/java.lang=ALL-UNNAMED \
     --add-opens=java.desktop/sun.awt=ALL-UNNAMED \
     --add-opens=java.desktop/java.awt=ALL-UNNAMED \
     -jar target/Shimeji-ee.jar
```

## 打包分发

### 创建便携版

```bash
# 使用 Maven profile
mvn -P jpackage-portable
```

生成的文件位于 `target/jpackage-windows-portable/`

### 创建 MSI 安装包

```bash
# Windows MSI 安装包
mvn -P jpackage-msi
```

生成的 MSI 文件位于 `target/jpackage-windows-msi/`

### 手动打包

如果自动打包失败，可以手动创建：

```bash
# 创建发布目录
mkdir release
cp target/Shimeji-ee.jar release/
cp -r conf/ release/
cp -r img/ release/
```

## 部署选项

### 本地部署

直接运行构建的 JAR 文件或安装 MSI 包。

### 企业部署

#### 静默安装

```powershell
# MSI 静默安装
msiexec /i Shimeji-ee.msi /quiet
```

#### 批量部署脚本

```batch
@echo off
echo 正在部署 Shimeji-Live...
msiexec /i Shimeji-ee.msi /quiet
echo 部署完成
```

### 便携式部署

将便携版解压到共享网络位置，用户可直接运行。

## CI/CD 流水线

### GitHub Actions

```yaml
name: Build and Release
on:
  push:
    tags: ['v*']

jobs:
  build:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '21'
      
      - name: Build with Maven
        run: mvn package
      
      - name: Create Release
        uses: actions/create-release@v1
        with:
          tag_name: ${{ github.ref }}
          release_name: Release ${{ github.ref }}
```

### 自动化测试

```bash
# 运行所有测试
mvn test

# 生成测试报告
mvn surefire-report:report
```

## 发布流程

### 版本管理

1. 更新 `pom.xml` 中的版本号
2. 创建 git 标签
3. 推送到远程仓库

```bash
# 更新版本
mvn versions:set -DnewVersion=2.1.0

# 创建标签
git tag v2.1.0
git push origin v2.1.0
```

### 发布检查清单

- [ ] 代码审查完成
- [ ] 测试通过
- [ ] 文档更新
- [ ] 版本号正确
- [ ] 构建产物测试
- [ ] 发布说明准备

## 故障排除

### 常见构建问题

1. **Java 版本不兼容**
   - 确保使用 Java 21+
   - 检查 `JAVA_HOME` 环境变量

2. **Maven 依赖问题**
   - 清理本地仓库：`mvn dependency:purge-local-repository`
   - 重新下载依赖：`mvn clean install`

3. **JPackage 失败**
   - 确保安装了 WiX Toolset（Windows）
   - 检查 PATH 环境变量

### 调试构建

```bash
# 详细输出
mvn -X package

# 跳过测试
mvn package -DskipTests

# 强制更新依赖
mvn clean install -U
```

## 性能优化

### 构建优化

```bash
# 并行构建
mvn -T 4 package

# 离线模式（跳过依赖检查）
mvn -o package
```

### 分发优化

- 压缩图像资源
- 移除不必要的依赖
- 使用模块化 JRE

更多详细信息请参考项目的构建脚本和 CI 配置。
