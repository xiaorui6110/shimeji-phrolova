# Shimeji-Live 文档站点

这是 Shimeji-Live 项目的官方文档站点，使用 VitePress 构建。

## 本地开发

```bash
# 安装依赖
cd docs-site
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览构建结果
npm run preview
```

## Docker 部署

```bash
# 构建镜像
docker build -t shimeji-live-docs .

# 运行容器
docker run -p 80:80 shimeji-live-docs

# 使用 docker-compose
docker-compose up -d
```

## 文档编写

- 用户文档位于 `user/` 目录
- 开发文档位于 `development/` 目录
- 配置文件位于 `.vitepress/config.js`

## 项目链接

- 主项目：https://github.com/DCRepairCenter/ShimejiLive
- 文档站点：部署在 GitHub Pages
