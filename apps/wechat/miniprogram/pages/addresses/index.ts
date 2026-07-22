import type { Address } from '@leisure-brew/api-contract'
import { customerApi } from '../../services/customer-api'

Page({
  data: { addresses: [] as Address[], selecting: false, loading: true },
  onLoad(options: Record<string, string>) {
    this.setData({ selecting: options.select === '1' })
  },
  async onShow() {
    await getApp<IAppOption>().sessionReady
    await this.load()
  },
  async load() {
    this.setData({ loading: true })
    try {
      this.setData({ addresses: await customerApi.addresses() })
    } finally {
      this.setData({ loading: false })
    }
  },
  add() {
    wx.navigateTo({ url: '/pages/address-edit/index' })
  },
  edit(event: WechatMiniprogram.TouchEvent) {
    wx.navigateTo({ url: `/pages/address-edit/index?id=${event.currentTarget.dataset.id}` })
  },
  async choose(event: WechatMiniprogram.TouchEvent) {
    if (!this.data.selecting) return
    await customerApi.setDefaultAddress(Number(event.currentTarget.dataset.id))
    wx.navigateBack()
  },
  async setDefault(event: WechatMiniprogram.TouchEvent) {
    await customerApi.setDefaultAddress(Number(event.currentTarget.dataset.id))
    await this.load()
  },
  async remove(event: WechatMiniprogram.TouchEvent) {
    const result = await wx.showModal({
      title: '删除这个地址？',
      content: '删除后无法恢复。',
      confirmText: '删除',
    })
    if (result.confirm) {
      await customerApi.deleteAddress(Number(event.currentTarget.dataset.id))
      await this.load()
    }
  },
})
