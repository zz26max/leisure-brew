import type { Category, Combo, MenuItem } from '@leisure-brew/api-contract'
import { customerApi } from '../../services/customer-api'
import { cartStore } from '../../store/cart'

type FlavorChoice = { name: string; options: string[]; selected: string }

Page({
  data: {
    shopOpen: true,
    loading: true,
    categories: [] as Category[],
    activeCategoryId: 0,
    items: [] as Array<MenuItem | Combo>,
    selectedItem: null as MenuItem | Combo | null,
    flavorChoices: [] as FlavorChoice[],
    sheetVisible: false,
    cartCount: 0,
    cartSubtotal: '0.00',
    skeletons: [1, 2, 3],
  },
  async onLoad() {
    await Promise.all([
      this.loadMenu(),
      this.loadShopStatus(),
      getApp<IAppOption>().sessionReady.then(() => this.refreshCart()),
    ])
  },
  async onShow() {
    await getApp<IAppOption>().sessionReady
    await this.refreshCart()
  },
  async loadShopStatus() {
    try {
      this.setData({ shopOpen: Number(await customerApi.shopStatus()) === 1 })
    } catch {
      this.setData({ shopOpen: false })
    }
  },
  async loadMenu() {
    this.setData({ loading: true })
    try {
      const categoryGroups = await Promise.all([customerApi.categories(1), customerApi.categories(2)])
      const categories = categoryGroups.flat()
      const activeCategoryId = categories[0]?.id || 0
      this.setData({ categories, activeCategoryId })
      if (activeCategoryId) await this.loadItems(activeCategoryId)
    } finally {
      this.setData({ loading: false })
    }
  },
  async loadItems(categoryId: number) {
    const category = this.data.categories.find((item) => item.id === categoryId)
    const items =
      category?.type === 2 ? await customerApi.combos(categoryId) : await customerApi.drinks(categoryId)
    this.setData({ items })
  },
  async selectCategory(event: WechatMiniprogram.TouchEvent) {
    const categoryId = Number(event.currentTarget.dataset.id)
    this.setData({ activeCategoryId: categoryId, loading: true })
    try {
      await this.loadItems(categoryId)
    } finally {
      this.setData({ loading: false })
    }
  },
  openItem(event: WechatMiniprogram.TouchEvent) {
    const item = this.data.items.find((product) => product.id === Number(event.currentTarget.dataset.id))
    if (!item) return
    const flavorChoices = ('specs' in item ? item.specs || [] : []).map((flavor) => {
      let options: string[] = []
      try {
        options = JSON.parse(flavor.value) as string[]
      } catch {
        options = []
      }
      return { name: flavor.name, options, selected: options[0] || '' }
    })
    this.setData({ selectedItem: item, flavorChoices, sheetVisible: true })
  },
  closeSheet() {
    this.setData({ sheetVisible: false })
  },
  noop() {},
  chooseFlavor(event: WechatMiniprogram.TouchEvent) {
    const index = Number(event.currentTarget.dataset.index)
    const value = String(event.currentTarget.dataset.value)
    this.setData({ [`flavorChoices[${index}].selected`]: value })
  },
  async addSelected() {
    const item = this.data.selectedItem
    if (!item || !this.data.shopOpen) return
    const dishFlavor = this.data.flavorChoices
      .map((choice) => choice.selected)
      .filter(Boolean)
      .join(',')
    const category = this.data.categories.find((entry) => entry.id === item.categoryId)
    await cartStore.add(category?.type === 2 ? { setmealId: item.id } : { dishId: item.id, dishFlavor })
    this.setData({ sheetVisible: false })
    this.syncCartView()
    wx.showToast({ title: '已放进购物袋', icon: 'success' })
  },
  async quickAdd(event: WechatMiniprogram.TouchEvent) {
    const item = this.data.items.find((product) => product.id === Number(event.currentTarget.dataset.id))
    if (!item || !this.data.shopOpen) return
    if ('specs' in item && item.specs?.length) {
      this.openItem(event)
      return
    }
    const category = this.data.categories.find((entry) => entry.id === item.categoryId)
    await cartStore.add(category?.type === 2 ? { setmealId: item.id } : { dishId: item.id })
    this.syncCartView()
  },
  async refreshCart() {
    try {
      await cartStore.refresh()
      this.syncCartView()
    } catch {
      this.setData({ cartCount: 0, cartSubtotal: '0.00' })
    }
  },
  syncCartView() {
    this.setData({ cartCount: cartStore.count, cartSubtotal: cartStore.subtotal.toFixed(2) })
  },
  goCart() {
    wx.switchTab({ url: '/pages/cart/index' })
  },
})
