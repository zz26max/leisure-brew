# 闲里茶咖小程序

这个目录是微信开发者工具可以直接打开的编译产物，并不是完整的 uni-app 源项目。页面可以联调、预览和上传，但在这里做的代码改动，未来重新执行 uni-app 构建时会被覆盖。

## 本地联调

默认接口地址是 `http://localhost:8080`。如果后端跑在 Docker、局域网主机或测试环境，在微信开发者工具控制台执行：

```js
wx.setStorageSync('leisureApiBaseUrl', 'http://你的后端地址:8080')
```

清除自定义地址：

```js
wx.removeStorageSync('leisureApiBaseUrl')
```

如需启用“联系门店”，再配置真实电话：

```js
wx.setStorageSync('leisureStorePhone', '你的门店电话')
```

未配置时不会再拨打教学项目里的示例号码，而是给出明确提示。

真机预览和正式发布必须使用已备案的 HTTPS 域名，并在微信公众平台配置 request 合法域名。发布前也要重新开启开发者工具的域名校验。

## 品牌样式

品牌覆盖集中在 `brand.wxss`，采用茶墨绿、焙火棕与米纸色。修改配色时从这里入手，不要在几十个编译后的页面样式里重复查找替换。

## 后续维护

建议尽快把原始 uni-app 工程补回仓库，并让 `miniprogram` 只作为构建输出。迁移时至少保留：

- `leisureApiBaseUrl` 的环境配置能力；
- `brand.wxss` 里的品牌变量与组件规则；
- 当前已经修正的页面文案。
