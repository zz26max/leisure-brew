import Vue from 'vue';
import Router from 'vue-router';
import Layout from '@/layout/index.vue';

Vue.use(Router);

const router = new Router({
  scrollBehavior: (to, from, savedPosition) => {
    if (savedPosition) {
      return savedPosition;
    }
    return { x: 0, y: 0 };
  },
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/login',
      component: () =>
        import(/* webpackChunkName: "login" */ '@/views/login/index.vue'),
      meta: { title: '闲里茶咖', hidden: true, notNeedAuth: true }
    },
    {
      path: '/404',
      component: () => import(/* webpackChunkName: "404" */ '@/views/404.vue'),
      meta: { title: '闲里茶咖', hidden: true, notNeedAuth: true }
    },
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          component: () =>
            import(/* webpackChunkName: "dashboard" */ '@/views/dashboard/index.vue'),
          name: 'Dashboard',
          meta: {
            title: '今日店况',
            icon: 'dashboard',
            affix: true
          }
        },
        {
          path: '/statistics',
          component: () =>
            import(/* webpackChunkName: "shopTable" */ '@/views/statistics/index.vue'),
          meta: {
            title: '经营报告',
            icon: 'icon-statistics'
          }
        },
        {
          path: 'order',
          component: () =>
            import(/* webpackChunkName: "shopTable" */ '@/views/orderDetails/index.vue'),
          meta: {
            title: '订单',
            icon: 'icon-order'
          }
        },
        {
          path: 'combo',
          component: () =>
            import(/* webpackChunkName: "shopTable" */ '@/views/combo/index.vue'),
          meta: {
            title: '搭配套餐',
            icon: 'icon-combo'
          }
        },
        {
          path: 'drink',
          component: () =>
            import(/* webpackChunkName: "shopTable" */ '@/views/drink/index.vue'),
          meta: {
            title: '饮品菜单',
            icon: 'icon-dish'
          }
        },
        {
          path: '/drink/add',
          component: () =>
            import(/* webpackChunkName: "shopTable" */ '@/views/drink/addDrink.vue'),
          meta: {
            title: '上新饮品',
            hidden: true
          }
        },

        {
          path: 'category',
          component: () =>
            import(/* webpackChunkName: "shopTable" */ '@/views/category/index.vue'),
          meta: {
            title: '菜单分组',
            icon: 'icon-category'
          }
        },
        {
          path: 'employee',
          component: () =>
            import(/* webpackChunkName: "shopTable" */ '@/views/employee/index.vue'),
          meta: {
            title: '门店团队',
            icon: 'icon-employee'
          }
        },

        {
          path: '/employee/add',
          component: () =>
            import(/* webpackChunkName: "dashboard" */ '@/views/employee/addEmployee.vue'),
          meta: {
            title: '添加伙伴',
            hidden: true
          }
        },

        {
          path: '/combo/add',
          component: () =>
            import(/* webpackChunkName: "shopTable" */ '@/views/combo/addCombo.vue'),
          meta: {
            title: '新建搭配',
            hidden: true
          }
        }
      ]
    },
    {
      path: '*',
      redirect: '/404',
      meta: { hidden: true }
    }
  ]
});

export default router;
