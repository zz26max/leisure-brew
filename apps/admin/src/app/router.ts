import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/features/auth/LoginPage.vue'), meta: { public: true } },
    {
      path: '/',
      component: () => import('@/app/AppShell.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          component: () => import('@/features/dashboard/DashboardPage.vue'),
          meta: { title: '今日店况' },
        },
        {
          path: 'orders',
          component: () => import('@/features/orders/OrdersPage.vue'),
          meta: { title: '订单工作台' },
        },
        {
          path: 'menu',
          component: () => import('@/features/menu/MenuPage.vue'),
          meta: { title: '菜单管理' },
        },
        {
          path: 'employees',
          component: () => import('@/features/employees/EmployeesPage.vue'),
          meta: { title: '门店伙伴' },
        },
        {
          path: 'reports',
          component: () => import('@/features/reports/ReportsPage.vue'),
          meta: { title: '经营分析' },
        },
        {
          path: 'settings',
          component: () => import('@/features/settings/SettingsPage.vue'),
          meta: { title: '门店设置' },
        },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.token) return { path: '/login', query: { redirect: to.fullPath } }
  if (to.path === '/login' && auth.token) return '/dashboard'
  document.title = `${String(to.meta.title || '门店运营台')} · 闲里茶咖`
})

export default router
