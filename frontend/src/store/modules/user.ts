import { Action, getModule, Module, Mutation, VuexModule } from 'vuex-module-decorators'
import Cookies from 'js-cookie'
import { login, userLogout } from '@/api/employee'
import {
  getStoreId,
  getToken,
  getUserInfo,
  removeStoreId,
  removeToken,
  removeUserInfo,
  setStoreId,
  setToken,
  setUserInfo,
  StoredUser,
} from '@/utils/cookies'
import store from '@/store'

export interface IUserState {
  token: string
  name: string
  avatar: string
  storeId: string
  introduction: string
  userInfo: StoredUser
  roles: string[]
  username: string
}

@Module({ dynamic: true, store, name: 'user' })
class User extends VuexModule implements IUserState {
  public token = getToken() || ''
  public storeId = getStoreId() || ''
  public userInfo = getUserInfo() || {}
  public name = this.userInfo.name || ''
  public username = Cookies.get('username') || ''
  public avatar = ''
  public introduction = ''
  public roles: string[] = this.token ? ['admin'] : []

  @Mutation
  private SET_TOKEN(token: string) { this.token = token }

  @Mutation
  private SET_USER(user: StoredUser) {
    this.userInfo = { ...user }
    this.name = user.name || ''
  }

  @Mutation
  private SET_USERNAME(username: string) { this.username = username }

  @Mutation
  private SET_ROLES(roles: string[]) { this.roles = roles }

  @Mutation
  private SET_STORE_ID(storeId: string) { this.storeId = storeId }

  @Action
  public async Login(credentials: { username: string; password: string }) {
    const username = credentials.username.trim()
    const response = await login({ username, password: credentials.password })
    const result = response.data
    if (Number(result.code) !== 1) throw new Error(result.msg || '登录失败')

    const user = result.data as StoredUser
    this.SET_USERNAME(username)
    this.SET_TOKEN(user.token || '')
    this.SET_USER(user)
    this.SET_ROLES(['admin'])
    Cookies.set('username', username)
    setToken(user.token || '')
    setUserInfo(user)
    return result
  }

  @Action
  public ResetToken() {
    removeToken()
    removeUserInfo()
    this.SET_TOKEN('')
    this.SET_USER({})
    this.SET_ROLES([])
  }

  @Action
  public changeStore(data: { data: string; authorization: string }) {
    this.SET_STORE_ID(data.data)
    this.SET_TOKEN(data.authorization)
    setStoreId(data.data)
    setToken(data.authorization)
  }

  @Action
  public GetUserInfo() {
    const user = getUserInfo()
    if (!this.token || !user) throw new Error('登录信息已失效')
    this.SET_USER(user)
    this.SET_ROLES(['admin'])
    return user
  }

  @Action
  public async LogOut() {
    try {
      await userLogout()
    } finally {
      removeToken()
      removeUserInfo()
      removeStoreId()
      Cookies.remove('username')
      this.SET_TOKEN('')
      this.SET_USER({})
      this.SET_ROLES([])
      this.SET_STORE_ID('')
    }
  }
}

export const UserModule = getModule(User)
