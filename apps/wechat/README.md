# 顾客微信小程序

原生 TypeScript 微信小程序源工程，覆盖茶单、规格选择、购物袋、地址、结算、订单与个人中心。`miniprogram/` 是源码目录，不再直接修改仓库根目录下的旧编译产物。

1. 用微信开发者工具导入本目录。
2. 在调试器中设置接口地址：

```js
wx.setStorageSync('leisure-api-base-url', 'http://localhost:8080')
```

3. 开发环境需勾选“不校验合法域名”；正式发布前必须配置 HTTPS 合法域名和真实 AppID。
