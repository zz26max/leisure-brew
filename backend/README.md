# 闲里茶咖 (Leisure Brew) - 后端

一个功能完整的茶饮/咖啡售卖管理系统，采用前后端分离架构，包含管理后台和微信小程序用户端。

## 项目架构

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   管理端 (前端)    │     │   Spring Boot    │     │  微信小程序 (用户端) │
│  Vue + Element UI │────>│     后端服务      │<────│   (独立仓库)      │
│    端口: 8081     │     │    端口: 8080     │     │                 │
└─────────────────┘     └────────┬────────┘     └─────────────────┘
                                 │
                    ┌────────────┼────────────┐
                    │            │            │
                ┌───▼───┐  ┌────▼────┐  ┌────▼────┐
                │ MySQL │  │  Redis  │  │ 阿里云OSS │
                └───────┘  └─────────┘  └─────────┘
```

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.3 | 核心框架 |
| MyBatis | 2.2.0 | ORM 持久层框架 |
| Spring Data Redis | - | Redis 缓存支持 |
| Spring Cache | - | 缓存抽象层 |
| Spring WebSocket | - | 实时消息推送 |
| PageHelper | 1.3.0 | MyBatis 分页插件 |
| Druid | 1.2.1 | 数据库连接池 |
| Knife4j | 3.0.2 | Swagger API 文档 |
| JJWT | 0.9.1 | JWT 令牌认证 |
| Lombok | 1.18.20 | 代码简化工具 |
| Fastjson | 1.2.76 | JSON 序列化 |
| Apache POI | 3.16 | Excel 报表导出 |
| AspectJ | 1.9.4 | AOP 切面编程 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 2.6.x | 核心框架 |
| TypeScript | 3.6.2 | 类型安全的 JavaScript |
| Vue Router | 3.1.x | 前端路由 |
| Vuex | 3.1.x | 状态管理 |
| Element UI | 2.12.x | UI 组件库 |
| Axios | 0.19.x | HTTP 请求库 |
| ECharts | 5.3.x | 数据可视化图表 |
| SCSS | - | CSS 预处理器 |
| Vue CLI | 3.11.x | 项目构建工具 |

### 数据库 & 中间件

| 技术 | 说明 |
|------|------|
| MySQL | 关系型数据库，存储业务数据 |
| Redis | 缓存、会话管理 |
| 阿里云 OSS | 对象存储，管理饮品/套餐图片等静态资源 |

### 第三方服务

| 服务 | 说明 |
|------|------|
| 微信小程序登录 | 用户端微信授权登录 |
| 微信支付 | 在线支付功能 |
| Cpolar | 内网穿透，用于微信支付回调 |

## 项目结构

### 后端 (多模块 Maven 项目)

```
sky-take-out/
├── sky-common/          # 公共模块：工具类、常量、异常、配置属性
│   └── com.sky
│       ├── constant/    # 常量定义
│       ├── context/     # ThreadLocal 上下文
│       ├── enumeration/ # 枚举类
│       ├── exception/   # 自定义异常
│       ├── json/        # Jackson 配置
│       ├── properties/  # 配置属性类
│       ├── result/      # 统一响应封装
│       └── utils/       # 工具类 (JWT, OSS, HTTP, 微信支付)
│
├── sky-pojo/            # 实体模块：DTO、VO、Entity
│   └── com.sky
│       ├── dto/         # 数据传输对象 (22个)
│       ├── entity/      # 数据库实体类 (12个)
│       └── vo/          # 视图对象 (17个)
│
└── sky-server/          # 主服务模块：业务逻辑
    └── com.sky
        ├── annotation/  # 自定义注解 (@AutoFill)
        ├── aspect/      # AOP 切面 (自动填充审计字段)
        ├── config/      # 配置类 (WebMvc, Redis, OSS, WebSocket)
        ├── controller/  # 控制器
        │   ├── admin/   # 管理端接口 (10个)
        │   ├── user/    # 用户端接口 (8个)
        │   └── notify/  # 回调通知 (微信支付)
        ├── handler/     # 全局异常处理
        ├── interceptor/ # JWT 拦截器 (管理端 + 用户端)
        ├── mapper/      # MyBatis Mapper 接口 (12个)
        ├── service/     # 业务逻辑层 (12个接口)
        ├── talk/        # 定时任务
        └── websocket/   # WebSocket 服务
```

### 前端

```
frontend/
├── src/
│   ├── api/             # API 请求模块
│   ├── assets/          # 静态资源 (图片、音频)
│   ├── components/      # 公共组件 (面包屑、图表、上传等)
│   ├── icons/           # SVG 图标
│   ├── layout/          # 页面布局 (侧边栏、导航栏)
│   ├── router/          # 路由配置
│   ├── store/           # Vuex 状态管理
│   ├── styles/          # 全局样式 (SCSS)
│   ├── utils/           # 工具函数 (请求封装、Cookie、验证)
│   └── views/           # 页面视图
│       ├── login/       # 登录页
│       ├── dashboard/   # 工作台
│       ├── orderDetails/# 订单管理
│       ├── drink/       # 饮品管理
│       ├── combo/       # 套餐管理
│       ├── category/    # 分类管理
│       ├── employee/    # 员工管理
│       ├── statistics/  # 数据统计
│       └── inform/      # 消息通知
├── .env.development     # 开发环境配置
├── .env.production      # 生产环境配置
└── vue.config.js        # Vue CLI 配置
```

## 功能模块

| 模块 | 功能说明 |
|------|----------|
| 员工管理 | 员工登录/登出、增删改查、状态启禁用、密码修改 |
| 分类管理 | 菜品分类和套餐分类的增删改查、排序 |
| 菜品管理 | 菜品增删改查、口味管理、图片上传、启售/停售 |
| 套餐管理 | 套餐增删改查、关联菜品、启售/停售 |
| 订单管理 | 订单查询、接单、拒单、取消、配送、完成、订单统计 |
| 数据统计 | 营业额统计、订单统计、用户统计、商品销量 Top10 |
| 消息通知 | 新订单实时推送、消息已读/未读管理 |
| 购物车 | 用户端添加/清空购物车 |
| 地址簿 | 用户端收货地址管理 |

## 环境要求

| 环境 | 版本要求 |
|------|----------|
| JDK | 8+ |
| Maven | 3.6+ |
| Node.js | 12+ (推荐 14+) |
| MySQL | 5.7+ |
| Redis | 5.0+ |

## 快速启动

### 1. 数据库准备

```sql
-- 创建数据库
CREATE DATABASE meal_buddy DEFAULT CHARACTER SET utf8mb4;
-- 导入 SQL 脚本 (sql/ 目录下)
```

### 2. 启动后端

```bash
cd sky-take-out

# 修改数据库和 Redis 配置
# 编辑 sky-server/src/main/resources/application-dev.yml

# 编译并启动
mvn clean package -DskipTest
cd sky-server
mvn spring-boot:run
```

后端启动后访问：`http://localhost:8080`

### 3. 启动前端

```bash
cd ../frontend

# 安装依赖
npm install

# 启动开发服务器
npm run serve
```

前端启动后访问：`http://localhost:8081`

### 4. 默认登录

- 账号：`admin`
- 密码：`123456`

## API 文档

后端启动后，访问 Knife4j API 文档：

- 管理端接口：`http://localhost:8080/doc.html`（分组：管理端接口）
- 用户端接口：`http://localhost:8080/doc.html`（分组：用户端接口）

## 核心设计

- **双端 JWT 认证**：管理端和用户端使用不同的密钥和拦截器，互不影响
- **AOP 自动填充**：通过 `@AutoFill` 注解自动填充 `createTime`、`updateTime`、`createUser`、`updateUser`
- **ThreadLocal 上下文**：拦截器解析 JWT 后将用户 ID 存入 ThreadLocal，供业务层使用
- **WebSocket 推送**：新订单实时通知管理端
- **定时任务**：超时未支付订单自动取消（15分钟）、每日自动完成配送超时订单

## 项目预览

> 可在此处添加项目截图

<!-- ![登录页](screenshots/login.png) -->
<!-- ![工作台](screenshots/dashboard.png) -->
<!-- ![订单管理](screenshots/order.png) -->

## License

本项目仅供学习参考。
