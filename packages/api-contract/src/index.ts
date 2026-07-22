export interface ApiResponse<T = null> {
  code: number
  msg: string | null
  data: T
}

export interface PageResult<T> {
  total: number
  records: T[]
}

export interface AdminSession {
  id: number
  userName: string
  name: string
  token: string
}

export interface Employee {
  id: number
  username: string
  name: string
  phone: string
  sex: '0' | '1'
  idNumber?: string
  status: 0 | 1
  updateTime?: string
}

export interface Category {
  id: number
  type: 1 | 2
  name: string
  sort: number
  status: 0 | 1
  updateTime?: string
}

export interface DrinkSpec {
  id?: number
  drinkId?: number
  name: string
  value: string
}

export interface MenuItem {
  id: number
  name: string
  categoryId: number
  price: number
  image: string
  description?: string
  status: 0 | 1
  specs?: DrinkSpec[]
}

export interface Combo {
  id: number
  categoryId: number
  name: string
  price: number
  image: string
  description?: string
  status: 0 | 1
  comboDrinks?: ComboDrink[]
}

export interface ComboDrink {
  id?: number
  comboId?: number
  drinkId: number
  name?: string
  price?: number
  copies: number
}

export const OrderStatus = {
  PendingPayment: 1,
  PendingConfirmation: 2,
  Confirmed: 3,
  Delivering: 4,
  Completed: 5,
  Cancelled: 6,
} as const

export type OrderStatusValue = (typeof OrderStatus)[keyof typeof OrderStatus]

export const orderStatusText: Record<OrderStatusValue, string> = {
  1: '待付款',
  2: '待接单',
  3: '制作中',
  4: '配送中',
  5: '已完成',
  6: '已取消',
}

export interface OrderLine {
  id?: number
  dishId?: number
  setmealId?: number
  name: string
  image?: string
  amount: number
  number: number
  dishFlavor?: string
}

export interface Order {
  id: number
  number: string
  status: OrderStatusValue
  userId?: number
  orderTime: string
  checkoutTime?: string
  amount: number
  packAmount?: number
  consignee: string
  phone: string
  address: string
  remark?: string
  cancelReason?: string
  rejectionReason?: string
  orderDishes?: string
  orderDetailList?: OrderLine[]
}

export interface BusinessData {
  turnover: number
  validOrderCount: number
  orderCompletionRate: number
  unitPrice: number
  newUsers: number
}

export interface OrderOverview {
  waitingOrders: number
  deliveredOrders: number
  completedOrders: number
  cancelledOrders: number
  allOrders: number
}

export interface Address {
  id: number
  consignee: string
  sex: '0' | '1'
  phone: string
  provinceName?: string
  cityName?: string
  districtName?: string
  detail: string
  label?: string
  isDefault: 0 | 1
}

export interface CartLine {
  id?: number
  dishId?: number
  setmealId?: number
  name: string
  image: string
  amount: number
  number: number
  dishFlavor?: string
}
