import type { CartLine } from '@leisure-brew/api-contract'
import { customerApi } from '../services/customer-api'

let lines: CartLine[] = []

export const cartStore = {
  get lines() {
    return lines
  },
  get count() {
    return lines.reduce((sum, item) => sum + item.number, 0)
  },
  get subtotal() {
    return lines.reduce((sum, item) => sum + Number(item.amount) * item.number, 0)
  },
  async refresh() {
    lines = await customerApi.cart()
    return lines
  },
  async add(line: { dishId?: number; setmealId?: number; dishFlavor?: string }) {
    await customerApi.addCart(line)
    return this.refresh()
  },
  async subtract(line: { dishId?: number; setmealId?: number; dishFlavor?: string }) {
    await customerApi.subtractCart(line)
    return this.refresh()
  },
  async clear() {
    await customerApi.clearCart()
    lines = []
  },
}
