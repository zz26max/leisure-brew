# 闲里茶咖 · Leisure Brew

一个面向社区茶饮门店的点单与经营系统。它保留“苍穹外卖”可靠的业务骨架，但产品叙事、视觉语言和前端工程已经重新设计：顾客端更像一间可以慢慢挑茶的小店，运营台更像门店伙伴每天愿意打开的工作桌。

视觉以纸张米色、茶汤绿、茉莉黄和少量陶桃色为主。设计借鉴东方茶饮品牌的克制留白与花香意象，但不复制具体品牌的商标、版式或素材。

## 当前工程

```text
apps/
  admin/          Vue 3 + TypeScript 门店运营台
  wechat/         原生 TypeScript 微信小程序源码
packages/
  api-contract/   两端共享的接口与业务类型
  design-tokens/  共享品牌色与设计变量
backend/          Spring Boot 服务与数据库脚本
frontend/         旧 Vue 2 管理端，仅用于迁移对照
miniprogram/      旧小程序编译产物，仅用于迁移对照
docs/             工程规范与长期决策
```

新功能只在 `apps/` 和 `packages/` 中开发。旧目录暂时保留，便于对照尚未迁完的边缘行为；不要再向其中追加业务代码。

## 开发约定

开始编码前先阅读 [工程协作与代码规范](./docs/ENGINEERING_GUIDE.md)。它记录了 Git 分支与提交规则、TypeScript/Vue/小程序代码风格、接口边界、视觉语言、测试门槛和迁移策略，是上下文压缩后继续工作的依据。当前完成度、验证结果与发布前事项见 [前端重写进度](./docs/FRONTEND_REWRITE.md)。

环境要求：Node.js 22 LTS 或 24、pnpm 11.7、JDK 17、Maven 3.9、MySQL 8、Redis 7。

```powershell
corepack enable
pnpm install
pnpm dev:admin
```

管理端开发地址为 `http://localhost:5173`，Vite 会把 `/api` 和 `/ws` 代理到本机 `8080` 端口。

小程序用微信开发者工具导入 `apps/wechat`。本地接口地址可在调试器中设置：

```js
wx.setStorageSync('leisure-api-base-url', 'http://localhost:8080')
```

## 一键启动

```powershell
Copy-Item .env.example .env
docker compose config
docker compose up --build -d
```

请先在 `.env` 中填写 MySQL、Redis 和两组不同的 JWT 随机密钥。启动后：

- 门店运营台：`http://localhost`
- 接口文档：`http://localhost:8080/doc.html`
- 健康检查：`http://localhost:8080/actuator/health`

本地演示账号为 `admin`，初始口令是 `LocalBrew@2026!`。首次登录后请修改，正式环境不要复用。

## 数据、支付与安全

空数据卷会依次执行 `schema.sql`、`demo-data.sql`、`local-admin.sql` 和 `indexes.sql`。`cleanup-load-test.sql` 只供手动维护，不随启动执行。

`PAYMENT_SIMULATION_ENABLED=true` 只用于本地走通下单，不会发起真实扣款。生产环境必须保持为 `false`，并在接入支付机构官方 SDK、证书验签和回调幂等后再开放。

## 质量检查

```powershell
pnpm format:check
pnpm lint
pnpm typecheck
pnpm test
pnpm build

Set-Location backend
mvn test
```

管理端、小程序和共享包必须全部通过检查。提交前不要把 `dist/`、微信编译输出、真实密钥或本地 `.env` 放进 Git。
