import Cookies from 'js-cookie'

export interface StoredUser {
  id?: number
  name?: string
  userName?: string
  token?: string
  roles?: string[]
  avatar?: string
  introduction?: string
}

const SIDEBAR_KEY = 'sidebar_status'
const TOKEN_KEY = 'token'
const USER_KEY = 'user_info'
const STORE_KEY = 'storeId'

export const getSidebarStatus = () => Cookies.get(SIDEBAR_KEY)
export const setSidebarStatus = (status: string) => Cookies.set(SIDEBAR_KEY, status)

export const getToken = () => Cookies.get(TOKEN_KEY)
export const setToken = (token: string) => Cookies.set(TOKEN_KEY, token)
export const removeToken = () => Cookies.remove(TOKEN_KEY)

export const getStoreId = () => Cookies.get(STORE_KEY)
export const setStoreId = (id: string) => Cookies.set(STORE_KEY, id)
export const removeStoreId = () => Cookies.remove(STORE_KEY)

export const getUserInfo = (): StoredUser | null => {
  const value = Cookies.get(USER_KEY)
  if (!value) return null
  try {
    return JSON.parse(value) as StoredUser
  } catch (error) {
    Cookies.remove(USER_KEY)
    return null
  }
}
export const setUserInfo = (user: StoredUser) =>
  Cookies.set(USER_KEY, JSON.stringify(user))
export const removeUserInfo = () => Cookies.remove(USER_KEY)

const PRINT_KEY = 'print'
export const getPrint = () => Cookies.get(PRINT_KEY)
export const setPrint = (value: object) => Cookies.set(PRINT_KEY, JSON.stringify(value))
export const removePrint = () => Cookies.remove(PRINT_KEY)

const NOTICE_KEY = 'new'
export const getNewData = () => Cookies.get(NOTICE_KEY)
export const setNewData = (value: object) => Cookies.set(NOTICE_KEY, JSON.stringify(value))
