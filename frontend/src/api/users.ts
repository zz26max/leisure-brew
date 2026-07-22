import request from '@/utils/request'

export const editPassword = (data: { oldPassword: string; newPassword: string }) =>
  request({ url: '/employee/editPassword', method: 'put', data })

export const getStatus = () =>
  request({ url: '/shop/status', method: 'get' })

export const setStatus = (status: number) =>
  request({ url: `/shop/${status}`, method: 'put' })
