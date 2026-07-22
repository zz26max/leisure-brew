import axios from 'axios'
import { Message } from 'element-ui'
import router from '@/router'
import { UserModule } from '@/store/modules/user'
import { checkPending, getRequestKey, pending, removePending } from './requestOptimize'

const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API,
  timeout: 30000,
})

service.interceptors.request.use(
  (config: any) => {
    if (UserModule.token) {
      config.headers = config.headers || {}
      config.headers.token = UserModule.token
    }

    const key = getRequestKey(config)
    if (checkPending(key)) {
      const source = axios.CancelToken.source()
      config.cancelToken = source.token
      source.cancel('请勿重复提交')
    } else {
      pending[key] = true
    }
    return config
  },
  (error: any) => Promise.reject(error)
)

service.interceptors.response.use(
  async (response: any) => {
    removeRequest(response.config)
    if (response.data && Number(response.data.code) === 0) {
      Message.error(response.data.msg || '操作没有完成，请稍后再试')
    }
    return response
  },
  async (error: any) => {
    removeRequest(error && error.config)
    if (axios.isCancel(error)) return Promise.reject(error)

    if (error && error.response && error.response.status === 401) {
      await UserModule.ResetToken()
      if (router.currentRoute.path !== '/login') {
        await router.replace({ path: '/login', query: { redirect: router.currentRoute.fullPath } })
      }
      Message.warning('登录已过期，请重新登录')
    } else if (!error || !error.response) {
      Message.error('暂时无法连接服务，请检查网络后重试')
    }
    return Promise.reject(error)
  }
)

function removeRequest(config?: any) {
  if (config) removePending(getRequestKey(config))
}

export default service
