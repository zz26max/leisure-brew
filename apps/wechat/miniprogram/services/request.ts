import type { ApiResponse } from '@leisure-brew/api-contract'

const BASE_URL_KEY = 'leisure-api-base-url'

export function apiBaseUrl() {
  return wx.getStorageSync<string>(BASE_URL_KEY) || 'http://localhost:8080'
}

export function request<T>(options: WechatMiniprogram.RequestOption): Promise<T> {
  const token = wx.getStorageSync<string>('customer-token')
  return new Promise<T>((resolve, reject) => {
    wx.request<ApiResponse<T>>({
      ...options,
      url: `${apiBaseUrl()}${options.url}`,
      timeout: 15_000,
      header: {
        'content-type': 'application/json',
        ...(token ? { authorization: token } : {}),
        ...options.header,
      },
      success(response) {
        if (response.statusCode === 401) {
          wx.removeStorageSync('customer-token')
          reject(new Error('登录已过期'))
          return
        }
        if (response.statusCode < 200 || response.statusCode >= 300) {
          reject(new Error('服务暂时不可用'))
          return
        }
        if (Number(response.data.code) !== 1) {
          reject(new Error(response.data.msg || '操作没有完成'))
          return
        }
        resolve(response.data.data)
      },
      fail: () => reject(new Error('网络开小差了，请稍后再试')),
    })
  }).catch((error: unknown) => {
    const message = error instanceof Error ? error.message : '网络开小差了，请稍后再试'
    wx.showToast({ title: message, icon: 'none' })
    throw error
  })
}
