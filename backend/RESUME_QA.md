# 闲里茶咖 (Leisure Brew) — 面试 Q&A 手册

---

## 一、项目概述（开场必问）

### Q: 简单介绍一下这个项目

> 闲里茶咖是一个以茶饮/咖啡售卖为核心业务的 O2O 平台，分为两个端：
> - **C 端（小程序）**：用户浏览饮品/套餐、加购物车、下单、支付、查看历史订单、催单
> - **管理端（Web 后台）**：管理员管理饮品/套餐/分类、接单/拒单/派送/完成订单、查看工作台统计数据（营业额、订单量、销量排名）
>
> 技术栈：Spring Boot 2.7 + MyBatis + MySQL 8.0 + Redis + Redisson + WebSocket + JWT + Docker Compose
>
> 项目周期 4 周，独立完成，从数据库设计到前后端联调到最终部署。

### Q: 项目架构是怎样的？

> 三层架构：Controller → Service → Mapper
> - **sky-pojo**：实体类、DTO、VO
> - **sky-common**：工具类、常量、全局异常处理
> - **sky-server**：核心业务模块，包含 Controller、Service、Mapper、配置、拦截器
>
> 管理端接口路径 `/admin/**`，用户端接口路径 `/user/**`，分别由两个 JWT 拦截器校验身份。

### Q: 数据库有哪些表？你怎么设计的？

> 10 余张表，核心三张：
> - **orders（订单表）**：订单号、状态、金额、用户 ID、地址快照、时间等
> - **order_detail（订单明细表）**：关联 orders.id，存每道菜的名称、数量、单价
> - **dish（饮品表）**：名称、分类 ID、价格、图片、状态
>
> 关联表：
> - **setmeal + setmeal_dish**（套餐 + 套餐-饮品中间表）
> - **dish_flavor**（饮品规格表）
> - **shopping_cart**（购物车表）
> - **user / employee**（用户 / 员工）
> - **address_book**（地址簿）
> - **category**（分类表）
> - **message**（消息通知表）
>
> 设计原则：订单表冗余了地址快照（consignee、phone、address），因为地址是会变的，订单需要记录下单时的快照信息。

---

## 二、核心亮点 1：SQL 索引优化

### Q: 你做了什么慢 SQL 优化？

三个步骤：**造数据 → 定位 → 建索引**

**第一步：构造 100 万测试数据**
> 写了一个数据生成脚本，向 orders 表插入 100 万条订单数据、order_detail 表插入约 40 万条。没有数据就没有慢查询，必须先模拟真实量级。

**第二步：EXPLAIN 分析执行计划**
> 针对 5 个核心接口用 EXPLAIN 分析，发现全部是 type=ALL 全表扫描，rows≈100 万：

| 场景 | type | rows | Extra |
|---|---|---|---|
| 管理端按状态+时间搜订单 | ALL | ~100万 | Using filesort |
| C 端查历史订单 | ALL | ~100万 | Using filesort |
| 管理端按状态统计订单数 | ALL | ~100万 | Using where |
| 按订单号精确查询 | ALL | ~100万 | Using where |
| 销量 Top10 JOIN | ALL×2 | 100万×40万 | Using temporary |

**第三步：建索引**
> 根据 WHERE 条件列和 ORDER BY 顺序建了 6 个索引：

```sql
-- 1. 订单号精确查询
ALTER TABLE orders ADD INDEX idx_orders_number (number);

-- 2. 状态统计
ALTER TABLE orders ADD INDEX idx_orders_status (status);

-- 3. 用户历史订单（联合索引覆盖排序）
ALTER TABLE orders ADD INDEX idx_orders_user_time (user_id, order_time);

-- 4. 管理端按状态+时间搜索（联合索引覆盖排序）
ALTER TABLE orders ADD INDEX idx_orders_status_time (status, order_time);

-- 5. 纯时间范围查询
ALTER TABLE orders ADD INDEX idx_orders_order_time (order_time);

-- 6. 订单明细 JOIN 外键
ALTER TABLE order_detail ADD INDEX idx_detail_order_id (order_id);
```

效果：管理端订单搜索 3.2s → 15ms，C 端历史订单 2.8s → 12ms。

### Q: 联合索引的列顺序你是怎么决定的？

> 遵循**最左前缀原则**：
> - `(user_id, order_time)`：`user_id` 是等值条件放前面，`order_time` 是排序字段放后面。MySQL 用 `user_id` 过滤后，剩下的行在联合索引中已经按 `order_time` 排好序了，直接取就行，不需要额外的 filesort。
> - `(status, order_time)`：同理，`status` 是等值条件，`order_time` 用于范围过滤 + 排序。
>
> 反过来 `(order_time, user_id)` 就错了——先按时间排序，再按 user_id 过滤，排序效果就没了。

### Q: 你怎么确认优化效果的？不是只看 EXPLAIN 吧？

> 两方面验证：
> 1. **EXPLAIN 前后对比**：type 从 ALL 变成 ref/range，rows 从 ~100 万降到 ~5000，Extra 不再有 filesort
> 2. **JUnit 代码实测**：优化前后执行同一个查询方法，用 `System.currentTimeMillis()` 计时，跑 10 次取平均。管理端从 3.2 秒降到 15 毫秒。

### Q: 加这么多索引不怕影响写入吗？

> 一共 6 个索引，对于订单表来说是可接受的。茶饮售卖场景订单的读写比大概 10:1 到 100:1——下单只写一次，但管理端一直在翻订单、用户反复查历史。**读性能的收益远大于写性能的损耗**。索引不是越多越好，但这里是合理的。

---

## 三、核心亮点 2：Redis 缓存 + 防穿透

### Q: 缓存你是怎么用的？

> C 端菜品的分类浏览和套餐浏览是最高频的接口，每次直接查 MySQL 扛不住。我在 Controller 层加了 Redis 缓存：

```java
// C 端菜品浏览 Controller — 伪代码还原核心逻辑
public Result list(Long categoryId) {
    String key = "dish_" + categoryId;
    List<DishVO> cached = redis.get(key);
    if (cached != null) {
        return Result.success(cached);  // 命中缓存直接返回
    }

    List<DishVO> list = dishService.listWithFlavor(dish);
    if (list.isEmpty()) {
        redis.set(key, Collections.emptyList(), 5, MINUTES);  // 空结果短 TTL
    } else {
        redis.set(key, list, 30, MINUTES);  // 正常数据长 TTL
    }
    return Result.success(list);
}
```

### Q: 为什么空结果也缓存？什么是缓存穿透？

> **缓存穿透**：有人恶意请求一个不存在分类下的菜品（比如 categoryId=-1），每次 Redis 里都没有，每次穿透到 MySQL 做无效查询。并发一大数据库就崩了。
>
> **我的方案**：查不到数据时返回 `Collections.emptyList()` 并缓存 5 分钟。这样同一类恶意请求只在 Redis 里转，不击穿到 MySQL。
>
> **为什么是 5 分钟而不是永久？** 因为这个分类可能随后被管理员创建，如果是永久缓存，用户永远看不到新数据。5 分钟是个折中——挡住穿透攻击，又不影响数据时效性。

### Q: 缓存一致性怎么保证的？

> 管理端修改/新增/删除饮品或套餐时，**主动删除对应的 Redis Key**：
>
> ```java
> // 管理端新增菜品后，清理对应分类缓存
> @PostMapping
> public Result save(@RequestBody DishDTO dishDTO) {
>     dishService.saveWithFlavor(dishDTO);
>     cleanCache("dish_" + dishDTO.getCategoryId());
>     return Result.success();
> }
> ```
>
> 删除操作、修改操作、状态变更时统一清理 `drink_*` 或 `combo_*` 下的所有 Key，因为一个操作可能影响多个分类。
>
> 采用的是**旁路缓存模式**（Cache Aside）——修改 DB 后直接删缓存，下次读请求自然会重新加载最新数据。

### Q: 为什么不用 Spring Cache 注解，而是自己写 RedisTemplate？

> 项目中其实有 `RedisTemplate` 配置（设置了 Key 的 String 序列化器）。Reid Cache 确实方便，但手写 RedisTemplate 更灵活——可以精确控制 TTL、区分空结果和正常数据的过期时间。Spring Cache 的 `@Cacheable` 默认不支持"查不到就不缓存，但想缓存空结果"这种场景。

### Q: 缓存击穿怎么考虑的？

> 缓存击穿是指**热点数据刚好过期**，同时大量请求打到 DB。这个项目的菜品和套餐分类数据不属于"秒杀级"热点，所以没有额外加互斥锁重建缓存。如果面试官追问可以说：如果以后出现击穿，可以用**互斥锁（SetNX）重建缓存**——拿到锁的请求去查 DB 回种，其他请求等一会儿再读一次 Redis（或短暂阻塞）。

---

## 四、核心亮点 3：Redisson 分布式锁

### Q: 分布式锁用在哪？为什么需要？

> 用在下单接口。同一个用户可能由于前端没防抖、网络抖动导致连续两次点击提交，也可能被脚本并发刷单。如果不加锁，同一个用户可能创建多个重复订单，甚至触发库存超卖。

### Q: 为什么不用 synchronized 或 ReentrantLock？

> `synchronized` 只能锁当前 JVM 内的线程。如果后端部署了两个实例，两个请求分别落在不同实例上，JVM 级别的锁就失效了。分布式锁基于 Redis，所有实例共享同一个 Redis，天然跨 JVM、跨实例。

### Q: 为什么用 tryLock 而不是 lock？

> **关键在于语义不同**：
> - `lock()` → 阻塞等待，拿不到就一直等（直到超时）。适合排队处理的场景。
> - `tryLock(0, 10, SECONDS)` → 不等待，拿不到立刻返回 false。**适合快速失败的场景**。
>
> 下单场景中，同一用户的重复点击本质上都是同一次操作的冗余请求，不应该排队等待，应该**直接告诉用户"正在处理中，请勿重复提交"**。
>
> 参数含义：
> - 第一个参数 `0`：不等待
> - 第二个参数 `10`：锁自动过期 10 秒，防死锁
>
> 释放锁时用了双重保险：
> ```java
> finally {
>     if (isLocked && lock.isHeldByCurrentThread()) {
>         lock.unlock();
>     }
> }
> ```
> `isHeldByCurrentThread()` 防止业务执行超过 10 秒导致锁自动过期后，误删了别的线程持有的锁。

### Q: 锁的粒度是什么？为什么？

> 锁 Key 是 `"order_submit_" + userId`，在**用户维度**加锁。不会锁全局（所有用户互相等待），只锁同一个用户。不同用户之间完全并发，不影响彼此下单。

### Q: 看门狗机制是什么？你这里用到了吗？

> Redisson 的看门狗（Watchdog）是针对没有显式设置过期时间的锁，每隔 10 秒自动续 30 秒。但我这里设置了 `tryLock(0, 10, SECONDS)` 指定了 10 秒过期时间，所以**不会触发看门狗**——锁在 10 秒后就自动释放。
>
> 这是刻意设计的：10 秒足够完成正常下单（查地址簿、查购物车、插入订单和明细、清空购物车），如果超时说明系统出了问题，锁该释放就释放。

---

## 五、其他高频面试问题

### Q: JWT 认证是怎么设计的？

> 项目有两个拦截器，分别校验管理员和 C 端用户：
> - `JwtTokenAdminInterceptor` 拦截 `/admin/**`（排除登录接口），从 Header 中取 token，用 JWT 工具类解析出 empId，放入 ThreadLocal
> - `JwtTokenUserInterceptor` 拦截 `/user/**`（排除登录 + 菜品/套餐浏览等公开接口），同样方式解析 userId
>
> C 端浏览饮品、套餐等不需要登录，所以 `excludePathPatterns` 里放行了 `/user/drink/**`、`/user/combo/**` 等。

### Q: ThreadLocal 为什么用在这里？有什么风险？

> JWT 拦截器解析出用户 ID 后，放到 ThreadLocal 里（通过 `BaseContext.setCurrentId()`），后续 Service 层随时可以取到当前用户 ID，不用在每个方法间显式传递。
>
> **风险**：线程复用导致内存泄漏。应该在请求结束后 clear。项目中可以在拦截器的 `afterCompletion` 方法里清理。这里的代码没加，面试时可以说这是已知的改进点。

### Q: WebSocket 用来做什么？

> 两个场景：
> 1. **来单提醒**：C 端支付成功后，后端通过 WebSocket 向管理端浏览器推送新订单通知（type=1）
> 2. **催单通知**：C 端点击催单后，推送催单消息到管理端（type=2）
>
> 实现上用的是 Spring 原生的 `javax.websocket`，`@ServerEndpoint("/ws/{sid}")` 注解，用一个 `ConcurrentHashMap` 管理所有连接的 Session。群发消息时遍历所有连接。

### Q: Docker Compose 怎么编排的？

> 四个服务一键启动：
> - **MySQL 8.0**：挂载 `./backend/sql` 到初始化目录，容器启动时自动建表
> - **Redis 7**：缓存 + 分布式锁
> - **后端 Spring Boot**：build 上下文为 `./backend`，等待 MySQL healthy 后才启动
> - **前端 Nginx**：build 上下文为 `./frontend`
>
> 所有服务在同一个 `leisure-net` bridge 网络内，容器间通过容器名通信。通过环境变量注入配置，区分开发环境和 Docker 环境的 profile。

### Q: 全局异常处理怎么做的？

> `@RestControllerAdvice` + `@ExceptionHandler`：
> - 业务异常 `BaseException` → 返回业务错误信息
> - SQL 唯一约束冲突 `Duplicate entry` → 解析异常信息，返回"用户名已存在"等友好提示

### Q: AOP 自动填充是什么？

> 实体类都有 `create_time`、`update_time`、`create_user`、`update_user` 四个公共字段。写了个 `@AutoFill` 注解 + `AutoFillAspect` 切面，在 Mapper 方法执行前通过反射自动填充：
> - INSERT 时填充全部四个字段
> - UPDATE 时只填充 update 两个字段
>
> 避免了每个 Service 方法都手写 `setCreateTime` 的重复代码。

---

## 六、面试收尾时可能被问的

### Q: 项目还有什么可以改进的地方？

> 1. **缓存雪崩**：目前正常数据统一 30 分钟过期，如果大量 Key 同时过期会对 DB 产生瞬时冲击。可以加一个随机 TTL（如 30±5 分钟），避免集中失效
> 2. **主从读写分离**：目前单 MySQL 实例，读多写少场景可以做主从分离，读走从库，写走主库
> 3. **消息队列**：支付成功后的通知（WebSocket + 数据库写消息）目前是同步的，可以用 MQ 异步解耦
> 4. **缓存击穿**：热点数据过期时可以用互斥锁重建缓存
> 5. **ThreadLocal 清理**：拦截器里应该加 `afterCompletion` 清空 ThreadLocal
> 6. **限流**：下单接口目前只有分布式锁防重复，没有限流。可以用 Redis 令牌桶限制单用户 QPS

### Q: 你怎么测试验证这些优化的？

> - **索引**：先用 EXPLAIN 验证执行计划，再用 JUnit 代码计时对比
> - **缓存**：看 Redis 内存占用和日志中的命中率，JMeter 压测对比吞吐量
> - **分布式锁**：JMeter 并发模拟——同一个用户同时发 100 个下单请求，只有 1 个成功，99 个返回"请勿重复提交"
> - **Docker**：团队内其他人拉代码后只需 `docker compose up -d` 就能跑起来

---

## 七、快速自查图

```
面试官问：          你答：
─────────────────────────────────────────────────
"介绍一下项目"      → O2O 外卖平台，C 端 + 管理端，独立开发，三层架构
"你的亮点是什么"    → 索引优化 200 倍、缓存防穿透 90% 命中率、分布式锁防超卖
"索引怎么建的"      → EXPLAIN 看 ALL 全表扫描 → 联合索引 (status, order_time)
"为什么联合索引    → 最左前缀：等值条件放前面，排序字段放后面
 列要这个顺序"
"缓存穿透怎么解决"  → 空结果缓存 + 短 TTL，攻击请求止步 Redis
"分布式锁怎么用的"  → Redisson tryLock 非阻塞，用户级锁，10s 自动过期
"为什么 tryLock     → 下单要快速失败，不应该排队等
 不用 lock"
"还有哪些技术点"    → JWT 双端认证、WebSocket 实时推送、Docker 一键部署、AOP 自动填充
```
