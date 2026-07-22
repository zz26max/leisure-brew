import request from '@/utils/request'

export const login = (data: { username: string; password: string }) =>
  request({ url: '/employee/login', method: 'post', data })

export const userLogout = () =>
  request({ url: '/employee/logout', method: 'post' })

export const getEmployeeList = (params: any) =>
  request({ url: '/employee/page', method: 'get', params })

export const enableOrDisableEmployee = (id: number, status: number) =>
  request({ url: `/employee/status/${status}`, method: 'post', params: { id } })

export const addEmployee = (data: any) =>
  request({ url: '/employee', method: 'post', data })

export const editEmployee = (data: any) =>
  request({ url: '/employee', method: 'put', data })

export const queryEmployeeById = (id: string | (string | null)[]) =>
  request({ url: `/employee/${id}`, method: 'get' })
