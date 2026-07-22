const path = require('path')

module.exports = {
  publicPath: '/',
  lintOnSave: process.env.NODE_ENV === 'development',
  pwa: { name: '闲里茶咖门店运营台' },
  pluginOptions: {
    'style-resources-loader': {
      preProcessor: 'scss',
      patterns: [
        path.resolve(__dirname, 'src/styles/_variables.scss'),
        path.resolve(__dirname, 'src/styles/_mixins.scss'),
      ],
    },
  },
  devServer: {
    host: '0.0.0.0',
    port: 8081,
    open: true,
    overlay: { warnings: false, errors: true },
    proxy: {
      '/api': {
        target: process.env.VUE_APP_URL || 'http://localhost:8080/admin',
        changeOrigin: true,
        pathRewrite: { '^/api': '' },
      },
      '/ws': {
        target: process.env.VUE_APP_WS_PROXY || 'ws://localhost:8080',
        ws: true,
        changeOrigin: true,
      },
    },
  },
  chainWebpack: config => config.resolve.symlinks(true),
  configureWebpack: { devtool: process.env.NODE_ENV === 'production' ? false : 'source-map' },
  css: { extract: true, sourceMap: false, loaderOptions: {}, modules: false },
}
