import { customerApi } from './services/customer-api'

App<IAppOption>({
  globalData: { user: null },
  sessionReady: Promise.resolve(),
  onLaunch() {
    this.sessionReady = this.refreshSession()
  },
  async refreshSession() {
    try {
      const login = await wx.login()
      const response = await customerApi.login(login.code)
      wx.setStorageSync('customer-token', response.token)
      this.globalData.user = response
    } catch {
      wx.removeStorageSync('customer-token')
    }
  },
})

declare global {
  interface IAppOption {
    globalData: { user: { id?: number; openid?: string; token: string } | null }
    sessionReady: Promise<void>
    refreshSession(): Promise<void>
  }
}
