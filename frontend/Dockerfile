# ---- 构建阶段 ----
FROM node:14 AS builder

WORKDIR /app
COPY package.json yarn.lock ./
RUN yarn config set registry https://registry.npmmirror.com && \
    sed -i 's|registry.npm.taobao.org|registry.npmmirror.com|g' yarn.lock && \
    yarn install --network-timeout 600000

COPY . .
RUN yarn build

# ---- 运行阶段 ----
FROM nginx:1.20-alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
