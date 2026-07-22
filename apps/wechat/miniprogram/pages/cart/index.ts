import type { CartLine } from '@leisure-brew/api-contract'
import { cartStore } from '../../store/cart'

Page({
  data: {
    loading: true,
    lines: [] as CartLine[],
    count: 0,
    subtotal: '0.00',
    packaging: '0.00',
    delivery: '6.00',
    total: '0.00',
  },
  async onShow() {
    await getApp<IAppOption>().sessionReady
    await this.load()
  },
  async load() {
    this.setData({ loading: true })
    try {
      await cartStore.refresh()
      this.sync()
    } finally {
      this.setData({ loading: false })
    }
  },
  sync() {
    const packaging = cartStore.count
    const total = cartStore.subtotal + packaging + 6
    this.setData({
      lines: cartStore.lines,
      count: cartStore.count,
      subtotal: cartStore.subtotal.toFixed(2),
      packaging: packaging.toFixed(2),
      total: total.toFixed(2),
    })
  },
  async add(event: WechatMiniprogram.TouchEvent) {
    const line = this.data.lines[Number(event.currentTarget.dataset.index)]
    await cartStore.add({ dishId: line.dishId, setmealId: line.setmealId, dishFlavor: line.dishFlavor })
    this.sync()
  },
  async subtract(event: WechatMiniprogram.TouchEvent) {
    const line = this.data.lines[Number(event.currentTarget.dataset.index)]
    await cartStore.subtract({ dishId: line.dishId, setmealId: line.setmealId, dishFlavor: line.dishFlavor })
    this.sync()
  },
  async clear() {
    const result = await wx.showModal({
      title: '清空购物袋？',
      content: '已经选好的饮品会全部移除。',
      confirmText: '清空',
    })
    if (result.confirm) {
      await cartStore.clear()
      this.sync()
    }
  },
  goMenu() {
    wx.switchTab({ url: '/pages/menu/index' })
  },
  checkout() {
    if (this.data.lines.length) wx.navigateTo({ url: '/pages/checkout/index' })
  },
})
