import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/app/router'

export const http = axios.create({
  baseURL: '/api/admin',
  timeout: 20_000,
})

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) config.headers.token = auth.token
  return config
})

http.interceptors.response.use(
  (response) => {
    if (response.data && Number(response.data.code) === 0) {
      ElMessage.error(response.data.msg || '操作没有完成，请稍后再试')
      return Promise.reject(new Error(response.data.msg || 'Business error'))
    }
    return response
  },
  async (error) => {
    if (error.response?.status === 401) {
      useAuthStore().clear()
      await router.replace({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
      ElMessage.warning('登录已过期，请重新登录')
    } else if (!error.response) {
      ElMessage.error('暂时无法连接门店服务')
    }
    return Promise.reject(error)
  },
)
