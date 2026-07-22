<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import BrandMark from '@/components/BrandMark.vue'
import { adminApi } from '@/services/admin-api'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const isOpen = ref(true)
const loadingStatus = ref(false)
const pageTitle = computed(() => String(route.meta.title || '门店运营台'))

const navigation = [
  { path: '/dashboard', label: '今日店况', mark: '今' },
  { path: '/orders', label: '订单工作台', mark: '单' },
  { path: '/menu', label: '菜单管理', mark: '茶' },
  { path: '/employees', label: '门店伙伴', mark: '人' },
  { path: '/reports', label: '经营分析', mark: '数' },
  { path: '/settings', label: '门店设置', mark: '设' },
]

let socket: WebSocket | null = null
let reconnectTimer: number | undefined
let reconnectCount = 0
let allowReconnect = true

async function loadShopStatus() {
  const response = await adminApi.shopStatus()
  isOpen.value = Number(response.data.data) === 1
}

async function toggleShop() {
  loadingStatus.value = true
  try {
    const next = isOpen.value ? 0 : 1
    await adminApi.setShopStatus(next)
    isOpen.value = next === 1
    ElMessage.success(isOpen.value ? '门店已恢复营业' : '门店已打烊')
  } finally {
    loadingStatus.value = false
  }
}

function connectSocket() {
  if (!auth.token || !auth.employeeId || !allowReconnect) return
  const protocol = location.protocol === 'https:' ? 'wss' : 'ws'
  socket = new WebSocket(
    `${protocol}://${location.host}/ws/${auth.employeeId}?token=${encodeURIComponent(auth.token)}`,
  )
  socket.onopen = () => {
    reconnectCount = 0
  }
  socket.onmessage = (event) => {
    try {
      const message = JSON.parse(event.data) as { type?: number; content?: string; orderId?: number }
      ElNotification({
        title: message.type === 1 ? '有一笔新订单' : '顾客正在催单',
        message: message.content || '请打开订单工作台查看。',
        duration: 0,
        onClick: () =>
          router.push({ path: '/orders', query: message.orderId ? { orderId: message.orderId } : {} }),
      })
    } catch {
      // 未知消息不影响当前页面。
    }
  }
  socket.onclose = (event) => {
    socket = null
    if (event.code === 1008) return
    const delay = Math.min(30_000, 2_000 * 2 ** reconnectCount++)
    reconnectTimer = window.setTimeout(connectSocket, delay)
  }
}

async function logout() {
  allowReconnect = false
  socket?.close()
  try {
    await adminApi.logout()
  } finally {
    auth.clear()
    await router.replace('/login')
  }
}

onMounted(() => {
  loadShopStatus()
  connectSocket()
})
onBeforeUnmount(() => {
  allowReconnect = false
  if (reconnectTimer) window.clearTimeout(reconnectTimer)
  socket?.close()
})
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="sidebar__brand">
        <BrandMark />
        <div><strong>闲里茶咖</strong><span>LEISURE BREW</span></div>
      </div>
      <nav aria-label="主导航">
        <RouterLink v-for="item in navigation" :key="item.path" :to="item.path">
          <span>{{ item.mark }}</span
          >{{ item.label }}
        </RouterLink>
      </nav>
      <div class="sidebar__quote">
        <span>今日小笺</span>
        <p>把每一杯认真做好，也把每一天从容打理。</p>
      </div>
    </aside>
    <main class="main-area">
      <header class="topbar">
        <div>
          <p>LEISURE BREW</p>
          <h2>{{ pageTitle }}</h2>
        </div>
        <div class="topbar__actions">
          <button :class="['shop-state', { closed: !isOpen }]" :disabled="loadingStatus" @click="toggleShop">
            <i />{{ isOpen ? '营业中' : '已打烊' }}
          </button>
          <el-dropdown trigger="click">
            <button class="partner-button">
              <span>{{ auth.displayName.slice(0, 1) }}</span
              >{{ auth.displayName }}
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/settings')">账号与门店设置</el-dropdown-item>
                <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <section class="page-content"><RouterView /></section>
    </main>
  </div>
</template>
