<template>
  <header class="topbar">
    <div class="topbar__page">
      <hamburger
        :is-active="sidebar.opened"
        class="topbar__hamburger"
        @toggleClick="toggleSideBar"
      />
      <div>
        <p class="topbar__eyebrow">
          LEISURE BREW
        </p>
        <h1>{{ pageTitle }}</h1>
      </div>
    </div>

    <div class="topbar__actions">
      <audio ref="newOrderAudio" hidden src="../../../assets/preview.mp3" />
      <audio ref="reminderAudio" hidden src="../../../assets/reminder.mp3" />
      <button :class="['store-status', { 'is-closed': status !== 1 }]" type="button" @click="openStatus">
        <span class="store-status__dot" />
        {{ status === 1 ? '营业中' : '已打烊' }}
        <i class="el-icon-arrow-right" />
      </button>
      <span class="topbar__divider" />
      <el-dropdown trigger="click" @command="handleUserCommand">
        <button type="button" class="user-menu">
          <span class="user-menu__avatar">{{ userInitial }}</span>
          <span>{{ name }}</span>
          <i class="el-icon-arrow-down" />
        </button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="password" icon="el-icon-key">
            修改密码
          </el-dropdown-item>
          <el-dropdown-item command="logout" icon="el-icon-switch-button" divided>
            退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>

    <el-dialog title="门店营业状态" :visible.sync="statusDialogVisible" width="420px">
      <el-radio-group v-model="nextStatus" class="status-options">
        <el-radio :label="1">
          <strong>营业中</strong><small>顾客可以浏览菜单并提交订单</small>
        </el-radio>
        <el-radio :label="0">
          <strong>已打烊</strong><small>暂停接收新订单，已有订单不受影响</small>
        </el-radio>
      </el-radio-group>
      <span slot="footer">
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveStatus">保存状态</el-button>
      </span>
    </el-dialog>

    <Password :dialog-form-visible="passwordDialogVisible" @handleclose="passwordDialogVisible = false" />
  </header>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import { AppModule } from '@/store/modules/app'
import { UserModule } from '@/store/modules/user'
import { getToken, getUserInfo } from '@/utils/cookies'
import Hamburger from '@/components/Hamburger/index.vue'
import { getStatus, setStatus } from '@/api/users'
import Password from '../components/password.vue'

@Component({ name: 'Navbar', components: { Hamburger, Password } })
export default class extends Vue {
  private websocket: WebSocket | null = null
  private reconnectTimer: number | null = null
  private reconnectAttempts = 0
  private shouldReconnect = true
  private statusDialogVisible = false
  private passwordDialogVisible = false
  private status = 1
  private nextStatus = 1

  get sidebar() { return AppModule.sidebar }
  get pageTitle() { return (this.$route.meta && this.$route.meta.title) || '门店运营台' }
  get currentUser() { return UserModule.userInfo.id ? UserModule.userInfo : (getUserInfo() || {}) }
  get name() { return this.currentUser.name || '门店伙伴' }
  get userInitial() { return this.name.trim().slice(0, 1) || '闲' }

  created() { this.connectWebSocket() }
  mounted() { this.loadStatus() }
  beforeDestroy() { this.disconnectWebSocket() }

  private connectWebSocket() {
    const token = getToken()
    const employeeId = this.currentUser.id
    if (!token || !employeeId || typeof WebSocket === 'undefined') return
    if (this.websocket && this.websocket.readyState < WebSocket.CLOSING) return

    this.websocket = new WebSocket(this.socketUrl(employeeId, token))
    this.websocket.onopen = () => { this.reconnectAttempts = 0 }
    this.websocket.onmessage = (event) => this.handleSocketMessage(event.data)
    this.websocket.onerror = () => undefined
    this.websocket.onclose = (event) => {
      this.websocket = null
      if (event.code === 1008) {
        this.shouldReconnect = false
        this.$notify.warning({ title: '实时提醒已断开', message: '登录已失效，请重新登录' })
      } else {
        this.scheduleReconnect()
      }
    }
  }

  private socketUrl(employeeId: number, token: string) {
    const configured = String(process.env.VUE_APP_SOCKET_URL || '').trim()
    const base = configured || `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/ws`
    return `${base.replace(/\/+$/, '')}/${employeeId}?token=${encodeURIComponent(token)}`
  }

  private handleSocketMessage(rawMessage: string) {
    let message: any
    try { message = JSON.parse(rawMessage) } catch (error) { return }
    const isNewOrder = Number(message.type) === 1
    const audio = this.$refs[isNewOrder ? 'newOrderAudio' : 'reminderAudio'] as HTMLAudioElement
    if (audio) { audio.currentTime = 0; audio.play().catch(() => undefined) }
    this.$notify({
      title: isNewOrder ? '有一笔新订单' : '顾客正在催单',
      message: message.content || (isNewOrder ? '请及时确认这笔订单。' : '请查看订单进度。'),
      duration: 0,
      onClick: () => this.$router.push({ path: '/order', query: message.orderId ? { orderId: String(message.orderId) } : {} }).catch(() => undefined),
    })
  }

  private scheduleReconnect() {
    if (!this.shouldReconnect || this.reconnectTimer) return
    const delay = Math.min(30000, 2000 * Math.pow(2, this.reconnectAttempts++))
    this.reconnectTimer = window.setTimeout(() => {
      this.reconnectTimer = null
      this.connectWebSocket()
    }, delay)
  }

  private disconnectWebSocket() {
    this.shouldReconnect = false
    if (this.reconnectTimer) window.clearTimeout(this.reconnectTimer)
    if (this.websocket) {
      this.websocket.onclose = null
      this.websocket.close()
      this.websocket = null
    }
  }

  private toggleSideBar() { AppModule.ToggleSideBar(false) }
  private handleUserCommand(command: string) {
    if (command === 'password') this.passwordDialogVisible = true
    if (command === 'logout') this.logout()
  }
  private async logout() {
    this.disconnectWebSocket()
    await UserModule.LogOut()
    await this.$router.replace('/login')
  }
  private async loadStatus() {
    const response = await getStatus()
    if (Number(response.data.code) === 1) this.status = this.nextStatus = Number(response.data.data)
  }
  private openStatus() { this.nextStatus = this.status; this.statusDialogVisible = true }
  private async saveStatus() {
    const response = await setStatus(this.nextStatus)
    if (Number(response.data.code) === 1) {
      this.status = this.nextStatus
      this.statusDialogVisible = false
      this.$message.success(this.status === 1 ? '门店已恢复营业' : '门店已打烊')
    }
  }
}
</script>

<style lang="scss" scoped>
.topbar { display: flex; align-items: center; justify-content: space-between; height: 72px; padding: 0 26px 0 18px; background: rgba(255, 252, 247, .96); border-bottom: 1px solid $color-border-light; }
.topbar__page, .topbar__actions, .store-status, .user-menu { display: flex; align-items: center; }
.topbar__page { gap: 13px; h1 { margin: 1px 0 0; font-family: 'Songti SC', STSong, SimSun, serif; font-size: 21px; } }
.topbar__eyebrow { margin: 0; color: $color-accent; font-size: 9px; font-weight: 700; letter-spacing: .2em; }
.topbar__hamburger { padding: 10px; cursor: pointer; }
.topbar__actions { gap: 14px; }
.topbar__divider { width: 1px; height: 26px; background: $color-border; }
.store-status, .user-menu { gap: 8px; height: 38px; border: 0; border-radius: 999px; cursor: pointer; font: inherit; }
.store-status { padding: 0 13px; color: $color-success; background: rgba(63, 122, 91, .09); }
.store-status__dot { width: 7px; height: 7px; background: $color-success; border-radius: 50%; }
.store-status.is-closed { color: $color-text-secondary; background: #eeeae1; .store-status__dot { background: $color-text-muted; } }
.user-menu { padding: 0 8px 0 3px; background: transparent; }
.user-menu__avatar { display: grid; width: 34px; height: 34px; color: #fffaf0; background: $color-primary; border-radius: 50%; place-items: center; }
.status-options { display: grid; gap: 12px; width: 100%; .el-radio { display: grid; grid-template-columns: 18px 1fr; margin: 0; padding: 18px; border: 1px solid $color-border; border-radius: $radius-md; white-space: normal; } strong, small { display: block; } small { margin-top: 7px; color: $color-text-secondary; } }
</style>
