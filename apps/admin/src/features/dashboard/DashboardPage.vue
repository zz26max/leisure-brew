<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { BusinessData, OrderOverview } from '@leisure-brew/api-contract'
import PageHeader from '@/components/PageHeader.vue'
import { adminApi } from '@/services/admin-api'

const loading = ref(true)
const business = ref<BusinessData>({
  turnover: 0,
  validOrderCount: 0,
  orderCompletionRate: 0,
  unitPrice: 0,
  newUsers: 0,
})
const overview = ref<OrderOverview>({
  waitingOrders: 0,
  deliveredOrders: 0,
  completedOrders: 0,
  cancelledOrders: 0,
  allOrders: 0,
})
const greeting = new Date().getHours() < 12 ? '早上好' : new Date().getHours() < 18 ? '下午好' : '晚上好'

onMounted(async () => {
  try {
    const [businessResponse, overviewResponse] = await Promise.all([
      adminApi.businessData(),
      adminApi.orderOverview(),
    ])
    business.value = businessResponse.data.data
    overview.value = overviewResponse.data.data
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading">
    <PageHeader
      eyebrow="TODAY AT A GLANCE"
      :title="`${greeting}，看看今天的店况`"
      description="先处理正在等待的客人，再慢慢看经营数据。"
    >
      <el-button type="primary" @click="$router.push('/orders')">打开订单工作台</el-button>
    </PageHeader>
    <section class="hero-card">
      <div>
        <span>今日营业额</span
        ><strong><small>¥</small>{{ Number(business.turnover || 0).toFixed(2) }}</strong>
        <p>
          完成 {{ business.validOrderCount || 0 }} 笔有效订单，客单价 ¥{{
            Number(business.unitPrice || 0).toFixed(2)
          }}
        </p>
      </div>
      <div class="hero-card__flower" aria-hidden="true">
        <i v-for="n in 6" :key="n" :style="{ transform: `rotate(${n * 60}deg) translateY(-34px)` }" />
      </div>
    </section>
    <section class="metric-grid">
      <article>
        <span>待接单</span><strong>{{ overview.waitingOrders || 0 }}</strong
        ><button @click="$router.push('/orders?status=2')">去处理 →</button>
      </article>
      <article>
        <span>配送中</span><strong>{{ overview.deliveredOrders || 0 }}</strong
        ><button @click="$router.push('/orders?status=4')">看进度 →</button>
      </article>
      <article>
        <span>已完成</span><strong>{{ overview.completedOrders || 0 }}</strong
        ><small>今天辛苦了</small>
      </article>
      <article>
        <span>完成率</span><strong>{{ Number(business.orderCompletionRate || 0).toFixed(1) }}%</strong
        ><small>保持从容节奏</small>
      </article>
    </section>
    <section class="dashboard-grid">
      <article class="paper-card">
        <p class="eyebrow">QUICK NOTES</p>
        <h3>今天先做这些</h3>
        <ul>
          <li :class="{ done: !overview.waitingOrders }"><i />及时确认待接订单</li>
          <li><i />留意低库存与下架商品</li>
          <li><i />打烊前核对配送中订单</li>
        </ul>
      </article>
      <article class="paper-card tea-tip">
        <span>茶香小记</span>
        <blockquote>好服务不是一味追赶，<br />而是每一步都让人安心。</blockquote>
        <p>闲里茶咖 · 门店手册</p>
      </article>
    </section>
  </div>
</template>
