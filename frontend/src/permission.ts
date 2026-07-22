import router from './router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { Route } from 'vue-router'
import { getToken } from '@/utils/cookies'

NProgress.configure({ showSpinner: false })

router.beforeEach((to: Route, _: Route, next: any) => {
  NProgress.start()
  if (getToken() || to.meta.notNeedAuth) {
    next()
  } else {
    next({ path: '/login', query: { redirect: to.fullPath } })
  }
})

router.afterEach((to: Route) => {
  NProgress.done()
  const pageTitle = to.meta && to.meta.title
  document.title = pageTitle ? `${pageTitle} · 闲里茶咖` : '闲里茶咖 · 门店运营台'
})
