# 门店运营台

基于 Vue 3、TypeScript、Vite、Pinia 与 Element Plus 的新管理端。页面视觉采用纸张米色、茶汤绿、茉莉黄与少量陶桃色，强调门店日常经营语境，而不是通用后台模板。

```powershell
pnpm install
pnpm dev:admin
```

开发服务器会把 `/api` 与 `/ws` 转发到 `http://localhost:8080`。生产构建使用同目录的 Dockerfile 和 Nginx 配置。
