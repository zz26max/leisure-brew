import type { Order } from '@leisure-brew/api-contract'
import { customerApi } from '../../services/customer-api'

const statusText: Record<number, string> = {
  1: '待付款',
  2: '等待接单',
  3: '正在制作',
  4: '配送中',
  5: '已完成',
  6: '已取消',
}

Page({
  data: {
    loading: true,
    orders: [] as Array<Order & { statusLabel: string }>,
    activeStatus: 0,
    page: 1,
    hasMore: true,
  },
  async onShow() {
    await getApp<IAppOption>().sessionReady
    await this.load(true)
  },
  async onPullDownRefresh() {
    try {
      await this.load(true)
    } finally {
      wx.stopPullDownRefresh()
    }
  },
  async onReachBottom() {
    if (this.data.hasMore) await this.load(false)
  },
  async load(reset: boolean) {
    if (this.data.loading && !reset) return
    const page = reset ? 1 : this.data.page
    this.setData({ loading: true })
    try {
      const response = await customerApi.orders(page, this.data.activeStatus || undefined)
      const next = response.records.map((order) => ({
        ...order,
        statusLabel: statusText[order.status] || '处理中',
      }))
      this.setData({
        orders: reset ? next : [...this.data.orders, ...next],
        page: page + 1,
        hasMore: next.length === 10,
      })
    } finally {
      this.setData({ loading: false })
    }
  },
  selectStatus(event: WechatMiniprogram.TouchEvent) {
    this.setData({ activeStatus: Number(event.currentTarget.dataset.status) })
    this.load(true)
  },
  open(event: WechatMiniprogram.TouchEvent) {
    wx.navigateTo({ url: `/pages/order-detail/index?id=${event.currentTarget.dataset.id}` })
  },
  async repeat(event: WechatMiniprogram.TouchEvent) {
    await customerApi.repeatOrder(Number(event.currentTarget.dataset.id))
    wx.switchTab({ url: '/pages/cart/index' })
  },
})
