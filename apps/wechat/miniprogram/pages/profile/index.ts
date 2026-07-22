Page({
  data: { avatarUrl: '', nickname: '爱喝茶的朋友' },
  onShow() {
    const profile = wx.getStorageSync<{ avatarUrl?: string; nickname?: string }>('customer-profile') || {}
    this.setData({ avatarUrl: profile.avatarUrl || '', nickname: profile.nickname || '爱喝茶的朋友' })
  },
  chooseAvatar(event: WechatMiniprogram.CustomEvent<{ avatarUrl: string }>) {
    const avatarUrl = event.detail.avatarUrl
    this.setData({ avatarUrl })
    wx.setStorageSync('customer-profile', { avatarUrl, nickname: this.data.nickname })
  },
  setNickname(event: WechatMiniprogram.Input) {
    const nickname = event.detail.value || '爱喝茶的朋友'
    this.setData({ nickname })
    wx.setStorageSync('customer-profile', { avatarUrl: this.data.avatarUrl, nickname })
  },
  goAddresses() {
    wx.navigateTo({ url: '/pages/addresses/index' })
  },
  goOrders() {
    wx.switchTab({ url: '/pages/orders/index' })
  },
})
