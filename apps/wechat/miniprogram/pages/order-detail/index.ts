import type { Order } from '@leisure-brew/api-contract'
import { customerApi } from '../../services/customer-api'

const statusText: Record<number, string> = {
  1: '等待付款',
  2: '等待门店接单',
  3: '门店正在制作',
  4: '正在送来的路上',
  5: '这一杯已送达',
  6: '订单已取消',
}

Page({
  data: { order: null as Order | null, statusLabel: '', created: false },
  async onLoad(options: Record<string, string>) {
    await getApp<IAppOption>().sessionReady
    this.setData({ created: options.created === '1' })
    const order = await customerApi.orderDetail(Number(options.id))
    this.setData({ order, statusLabel: statusText[order.status] || '订单处理中' })
  },
  async remind() {
    if (!this.data.order) return
    await customerApi.remindOrder(this.data.order.id)
    wx.showToast({ title: '已经提醒门店啦', icon: 'success' })
  },
  async cancel() {
    if (!this.data.order) return
    const result = await wx.showModal({ title: '确定取消订单？', content: '已经支付的订单会进入退款流程。' })
    if (result.confirm) {
      await customerApi.cancelOrder(this.data.order.id)
      wx.navigateBack()
    }
  },
  goHome() {
    wx.switchTab({ url: '/pages/menu/index' })
  },
})
