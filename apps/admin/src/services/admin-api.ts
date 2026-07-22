import type {
  AdminSession,
  ApiResponse,
  BusinessData,
  Category,
  Combo,
  Employee,
  MenuItem,
  Order,
  OrderOverview,
  PageResult,
} from '@leisure-brew/api-contract'
import { http } from './http'

export const adminApi = {
  login: (data: { username: string; password: string }) =>
    http.post<ApiResponse<AdminSession>>('/employee/login', data),
  logout: () => http.post<ApiResponse>('/employee/logout'),
  shopStatus: () => http.get<ApiResponse<number>>('/shop/status'),
  setShopStatus: (status: number) => http.put<ApiResponse>(`/shop/${status}`),
  businessData: () => http.get<ApiResponse<BusinessData>>('/workspace/businessData'),
  orderOverview: () => http.get<ApiResponse<OrderOverview>>('/workspace/overviewOrders'),
  orderStatistics: () => http.get<ApiResponse<Record<string, number>>>('/order/statistics'),
  orders: (params: Record<string, unknown>) =>
    http.get<ApiResponse<PageResult<Order>>>('/order/conditionSearch', { params }),
  orderDetail: (id: number) => http.get<ApiResponse<Order>>(`/order/details/${id}`),
  confirmOrder: (id: number) => http.put<ApiResponse>('/order/confirm', { id }),
  rejectOrder: (id: number, rejectionReason: string) =>
    http.put<ApiResponse>('/order/rejection', { id, rejectionReason }),
  cancelOrder: (id: number, cancelReason: string) =>
    http.put<ApiResponse>('/order/cancel', { id, cancelReason }),
  deliverOrder: (id: number) => http.put<ApiResponse>(`/order/delivery/${id}`),
  completeOrder: (id: number) => http.put<ApiResponse>(`/order/complete/${id}`),
  categories: (params: Record<string, unknown>) =>
    http.get<ApiResponse<PageResult<Category>>>('/category/page', { params }),
  drinks: (params: Record<string, unknown>) =>
    http.get<ApiResponse<PageResult<MenuItem>>>('/drink/page', { params }),
  combos: (params: Record<string, unknown>) =>
    http.get<ApiResponse<PageResult<Combo>>>('/combo/page', { params }),
  setDrinkStatus: (id: number, status: number) =>
    http.post<ApiResponse>(`/drink/status/${status}`, null, { params: { id } }),
  drink: (id: number) => http.get<ApiResponse<MenuItem>>(`/drink/${id}`),
  saveDrink: (data: Partial<MenuItem>) =>
    data.id ? http.put<ApiResponse>('/drink', data) : http.post<ApiResponse>('/drink', data),
  deleteDrink: (id: number) => http.delete<ApiResponse>('/drink', { params: { ids: id } }),
  setComboStatus: (id: number, status: number) =>
    http.post<ApiResponse>(`/combo/status/${status}`, null, { params: { id } }),
  saveCombo: (data: Partial<Combo>) => http.post<ApiResponse>('/combo', data),
  deleteCombo: (id: number) => http.delete<ApiResponse>('/combo', { params: { ids: id } }),
  saveCategory: (data: Partial<Category>) =>
    data.id ? http.put<ApiResponse>('/category', data) : http.post<ApiResponse>('/category', data),
  setCategoryStatus: (id: number, status: number) =>
    http.post<ApiResponse>(`/category/status/${status}`, null, { params: { id } }),
  deleteCategory: (id: number) => http.delete<ApiResponse>('/category', { params: { id } }),
  uploadImage: (file: File) => {
    const form = new FormData()
    form.append('file', file)
    return http.post<ApiResponse<string>>('/common/upload', form)
  },
  employees: (params: Record<string, unknown>) =>
    http.get<ApiResponse<PageResult<Employee>>>('/employee/page', { params }),
  addEmployee: (data: Omit<Employee, 'id' | 'status'>) => http.post<ApiResponse<string>>('/employee', data),
  updateEmployee: (data: Partial<Employee> & { id: number }) => http.put<ApiResponse>('/employee', data),
  setEmployeeStatus: (id: number, status: number) =>
    http.post<ApiResponse>(`/employee/status/${status}`, null, { params: { id } }),
  changePassword: (data: { oldPassword: string; newPassword: string }) =>
    http.put<ApiResponse>('/employee/editPassword', data),
  turnover: (begin: string, end: string) =>
    http.get<ApiResponse<{ dateList: string; turnoverList: string }>>('/report/turnoverStatistics', {
      params: { begin, end },
    }),
  top10: (begin: string, end: string) =>
    http.get<ApiResponse<{ nameList: string; numberList: string }>>('/report/top10', {
      params: { begin, end },
    }),
}
