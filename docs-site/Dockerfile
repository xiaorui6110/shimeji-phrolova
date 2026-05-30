# VitePress 多阶段构建 Dockerfile
FROM node:18-alpine AS builder
WORKDIR /app

# 安装 git，VitePress 需要它来获取文件时间戳
RUN apk add --no-cache git

COPY package*.json ./
RUN npm install
COPY . .

# 初始化 git 仓库以避免 git 错误
RUN git init && git add . && git commit -m "Initial commit" || true

RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/.vitepress/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
