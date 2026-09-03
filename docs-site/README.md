# Shimeji-Phrolova 文档站点

本项目（`shimeji-phrolova`）的官方文档站点，使用 [VitePress](https://vitepress.dev/) 构建。

## 本地开发

```bash
# 安装依赖（需要 Node.js 18+）
cd docs-site
npm install

# 启动开发服务器（浏览器打开 http://localhost:5173）
npm run dev

# 构建生产版本（输出到 .vitepress/dist）
npm run build

# 预览构建结果
npm run preview
```

## Docker 部署

```bash
# 构建镜像
docker build -t shimeji-phrolova-docs .

# 运行容器
docker run -p 80:80 shimeji-phrolova-docs

# 使用 docker-compose
docker-compose up -d
```

## 文档编写

- 用户文档位于 `user/` 目录
- 开发文档位于 `development/` 目录
- 站点配置（导航、侧边栏、搜索）位于 `.vitepress/config.js`
- 首页为 `index.md`

## 站点结构

```
docs-site/
├── index.md                # 首页
├── .vitepress/config.js    # 站点配置
├── user/                   # 用户指南
├── development/            # 开发文档
├── public/                 # 静态资源（图标等）
├── Dockerfile              # Docker 构建
└── docker-compose.yml      # Docker Compose
```

## 注意事项

- 文档中的 mermaid 图依赖 `vitepress-plugin-mermaid`，该插件随依赖自动安装。
- 图标资源暂缺：`public/` 为空，站点当前不展示 logo，待后续补充后可在 `config.js` 中引用。
