import type {
  Address,
  CartLine,
  Category,
  Combo,
  MenuItem,
  Order,
  PageResult,
} from '@leisure-brew/api-contract'
import { request } from './request'

export const customerApi = {
  login: (code: string) =>
    request<{ id?: number; openid?: string; token: string }>({
      url: '/user/user/login',
      method: 'POST',
      data: { code },
    }),
  shopStatus: () => request<number>({ url: '/user/shop/status', method: 'GET' }),
  categories: (type: Category['type']) =>
    request<Category[]>({ url: '/user/category/list', method: 'GET', data: { type } }),
  drinks: (categoryId: number) =>
    request<MenuItem[]>({ url: '/user/drink/list', method: 'GET', data: { categoryId } }),
  combos: (categoryId: number) =>
    request<Combo[]>({ url: '/user/combo/list', method: 'GET', data: { categoryId } }),
  cart: () => request<CartLine[]>({ url: '/user/shoppingCart/list', method: 'GET' }),
  addCart: (line: { dishId?: number; setmealId?: number; dishFlavor?: string }) =>
    request<null>({ url: '/user/shoppingCart/add', method: 'POST', data: line }),
  subtractCart: (line: { dishId?: number; setmealId?: number; dishFlavor?: string }) =>
    request<null>({ url: '/user/shoppingCart/sub', method: 'POST', data: line }),
  clearCart: () => request<null>({ url: '/user/shoppingCart/clean', method: 'DELETE' }),
  addresses: () => request<Address[]>({ url: '/user/addressBook/list', method: 'GET' }),
  defaultAddress: () => request<Address | null>({ url: '/user/addressBook/default', method: 'GET' }),
  address: (id: number) => request<Address>({ url: `/user/addressBook/${id}`, method: 'GET' }),
  saveAddress: (address: Partial<Address>) =>
    request<null>({ url: '/user/addressBook', method: address.id ? 'PUT' : 'POST', data: address }),
  setDefaultAddress: (id: number) =>
    request<null>({ url: '/user/addressBook/default', method: 'PUT', data: { id } }),
  deleteAddress: (id: number) => request<null>({ url: `/user/addressBook?id=${id}`, method: 'DELETE' }),
  submitOrder: (data: Record<string, unknown>) =>
    request<{ id: number; orderNumber: string; orderAmount: number; orderTime: string }>({
      url: '/user/order/submit',
      method: 'POST',
      data,
    }),
  payOrder: (orderNumber: string, payMethod = 1) =>
    request<Record<string, string>>({
      url: '/user/order/payment',
      method: 'PUT',
      data: { orderNumber, payMethod },
    }),
  orders: (page: number, status?: number) =>
    request<PageResult<Order>>({
      url: '/user/order/historyOrders',
      method: 'GET',
      data: { page, pageSize: 10, status },
    }),
  orderDetail: (id: number) => request<Order>({ url: `/user/order/orderDetail/${id}`, method: 'GET' }),
  cancelOrder: (id: number) => request<null>({ url: `/user/order/cancel/${id}`, method: 'PUT' }),
  remindOrder: (id: number) => request<null>({ url: `/user/order/reminder/${id}`, method: 'GET' }),
  repeatOrder: (id: number) => request<null>({ url: `/user/order/repetition/${id}`, method: 'POST' }),
}
