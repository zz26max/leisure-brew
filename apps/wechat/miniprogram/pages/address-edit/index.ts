import type { Address } from '@leisure-brew/api-contract'
import { customerApi } from '../../services/customer-api'

Page({
  data: {
    form: {
      id: undefined,
      consignee: '',
      sex: '1',
      phone: '',
      provinceName: '',
      cityName: '',
      districtName: '',
      detail: '',
      label: '家',
      isDefault: 0,
    } as Partial<Address>,
    region: [] as string[],
    regionText: '',
    labels: ['家', '公司', '学校'],
    saving: false,
  },
  async onLoad(options: Record<string, string>) {
    await getApp<IAppOption>().sessionReady
    if (options.id) {
      const form = await customerApi.address(Number(options.id))
      const region = [form.provinceName || '', form.cityName || '', form.districtName || '']
      this.setData({ form, region, regionText: region.join(' ') })
    }
  },
  setField(event: WechatMiniprogram.Input) {
    const field = String(event.currentTarget.dataset.field)
    this.setData({ [`form.${field}`]: event.detail.value })
  },
  setSex(event: WechatMiniprogram.TouchEvent) {
    this.setData({ 'form.sex': String(event.currentTarget.dataset.value) })
  },
  setLabel(event: WechatMiniprogram.TouchEvent) {
    this.setData({ 'form.label': String(event.currentTarget.dataset.value) })
  },
  setRegion(event: WechatMiniprogram.PickerChange) {
    const region = event.detail.value as string[]
    this.setData({
      region,
      regionText: region.join(' '),
      'form.provinceName': region[0],
      'form.cityName': region[1],
      'form.districtName': region[2],
    })
  },
  async save() {
    const form = this.data.form
    if (!form.consignee || !/^1\d{10}$/.test(form.phone || '') || !form.detail || !form.provinceName) {
      wx.showToast({ title: '请把地址信息填写完整', icon: 'none' })
      return
    }
    this.setData({ saving: true })
    try {
      await customerApi.saveAddress(form)
      wx.showToast({ title: '地址已保存', icon: 'success' })
      setTimeout(() => wx.navigateBack(), 500)
    } finally {
      this.setData({ saving: false })
    }
  },
})
