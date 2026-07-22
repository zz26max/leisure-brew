# 前端重写进度与接手说明

更新时间：2026-07-16

本文记录“闲里茶咖”前端重写的当前事实，供上下文压缩、换人接手或后续迭代时快速恢复现场。长期规则以 [ENGINEERING_GUIDE.md](./ENGINEERING_GUIDE.md) 为准。

## 设计方向

视觉参考新式东方茶饮品牌的克制留白、花香意象和日常化表达，但不复制古茗、茉莉奶白或其他品牌的商标、插画、具体版式与文案。

- 主色：深茶绿与嫩叶绿；
- 背景：纸张米色，不使用冷白后台底色；
- 点缀：茉莉黄与少量陶桃色；
- 形态：圆角茶器、花瓣、印章与纸笺；
- 文案：像门店伙伴说话，短、清楚、有温度，不使用教学项目占位语。

## 新工程入口

- `apps/admin`：Vue 3、TypeScript、Vite、Pinia、Vue Router、Element Plus、ECharts；
- `apps/wechat`：原生 TypeScript 微信小程序源码；
- `packages/api-contract`：两端共享的接口与业务类型；
- `packages/design-tokens`：共享品牌颜色；
- 根目录使用 pnpm workspace 统一执行质量检查。

旧 `frontend/` 和 `miniprogram/` 只保留作迁移对照，新需求不得继续写入旧目录。Docker Compose 的前端服务已经切到 `apps/admin/Dockerfile`。

## 已完成能力

管理端：

- 登录、会话恢复、401 退出与 WebSocket 新订单/催单提醒；
- 今日店况、订单筛选与完整状态流转；
- 饮品新增、编辑、图片上传、规格维护、上下架、删除；
- 套餐组合、新增、上下架、删除；
- 菜单分组新增、编辑、启停、删除；
- 员工新增、编辑、启停与一次性初始口令；
- 营业状态、经营趋势、热销排行和密码修改。

小程序：

- 微信登录会话屏障，避免启动时受保护请求早于登录；
- 饮品与套餐茶单、规格选择、购物袋；
- 地址新增、编辑、默认地址、删除；
- 订单确认、服务端计价、模拟支付、订单列表与详情；
- 催单、取消、再来一单和个人中心。

## 已执行检查

以下命令当前全部通过：

```powershell
pnpm format:check
pnpm lint
pnpm typecheck
pnpm test
pnpm build
```

管理端生产构建存在大分块提示，不影响构建成功；后续可通过 Element Plus 按需加载和 ECharts 模块化导入继续优化首屏体积。

Docker Compose 配置已成功解析，但本次环境的 Docker Desktop 守护进程未启动，因此没有完成镜像实建。Maven 3.9.11 已迁移到项目外并完成后端聚合构建、单模块编译和默认测试验证。

## 发布前仍需人工验证

1. 用真实微信 AppID 导入 `apps/wechat`，逐页检查不同机型安全区与授权流程；
2. 配置 HTTPS 合法域名、对象存储和图片上传；
3. 使用本地完整数据走一遍“上新—下单—接单—配送—完成”；
4. 关闭模拟支付，接入真实支付 SDK、证书验签和回调幂等后再发布；
5. 启动 Docker Desktop 后执行 `docker compose up --build` 验证容器链路。
