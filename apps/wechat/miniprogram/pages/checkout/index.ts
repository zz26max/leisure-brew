import type { Address, CartLine } from '@leisure-brew/api-contract'
import { customerApi } from '../../services/customer-api'
import { cartStore } from '../../store/cart'

Page({
  data: {
    address: null as Address | null,
    lines: [] as CartLine[],
    remark: '',
    count: 0,
    subtotal: '0.00',
    packaging: '0.00',
    total: '0.00',
    submitting: false,
  },
  async onShow() {
    await getApp<IAppOption>().sessionReady
    const [address] = await Promise.all([customerApi.defaultAddress(), cartStore.refresh()])
    const packaging = cartStore.count
    this.setData({
      address,
      lines: cartStore.lines,
      count: cartStore.count,
      subtotal: cartStore.subtotal.toFixed(2),
      packaging: packaging.toFixed(2),
      total: (cartStore.subtotal + packaging + 6).toFixed(2),
    })
  },
  chooseAddress() {
    wx.navigateTo({ url: '/pages/addresses/index?select=1' })
  },
  setRemark(event: WechatMiniprogram.Input) {
    this.setData({ remark: event.detail.value })
  },
  async submit() {
    if (!this.data.address) {
      wx.showToast({ title: '请先选择收货地址', icon: 'none' })
      return
    }
    if (this.data.submitting) return
    this.setData({ submitting: true })
    try {
      const result = await customerApi.submitOrder({
        addressBookId: this.data.address.id,
        payMethod: 1,
        remark: this.data.remark,
        deliveryStatus: 1,
        tablewareStatus: 1,
        tablewareNumber: this.data.count,
        amount: 0,
        packAmount: 0,
      })
      await customerApi.payOrder(result.orderNumber)
      wx.redirectTo({ url: `/pages/order-detail/index?id=${result.id}&created=1` })
    } finally {
      this.setData({ submitting: false })
    }
  },
})
