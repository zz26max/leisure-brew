<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Order, OrderStatusValue } from '@leisure-brew/api-contract'
import { OrderStatus, orderStatusText } from '@leisure-brew/api-contract'
import EmptyState from '@/components/EmptyState.vue'
import PageHeader from '@/components/PageHeader.vue'
import { adminApi } from '@/services/admin-api'

const route = useRoute()
const loading = ref(false)
const detailVisible = ref(false)
const selected = ref<Order | null>(null)
const orders = ref<Order[]>([])
const total = ref(0)
const query = reactive({
  page: 1,
  pageSize: 10,
  status: undefined as number | undefined,
  number: '',
  phone: '',
})
const statusTabs = [
  { label: '全部订单', value: undefined },
  { label: '待接单', value: OrderStatus.PendingConfirmation },
  { label: '制作中', value: OrderStatus.Confirmed },
  { label: '配送中', value: OrderStatus.Delivering },
  { label: '已完成', value: OrderStatus.Completed },
  { label: '已取消', value: OrderStatus.Cancelled },
]
const activeStatus = computed({
  get: () => (query.status === undefined ? 'all' : String(query.status)),
  set: (value) => {
    query.status = value === 'all' ? undefined : Number(value)
    query.page = 1
    loadOrders()
  },
})

async function loadOrders() {
  loading.value = true
  try {
    const response = await adminApi.orders({
      ...query,
      number: query.number || undefined,
      phone: query.phone || undefined,
    })
    orders.value = response.data.data.records
    total.value = response.data.data.total
  } finally {
    loading.value = false
  }
}
function searchOrders() {
  query.page = 1
  loadOrders()
}

async function openDetail(order: Order) {
  const response = await adminApi.orderDetail(order.id)
  selected.value = response.data.data
  detailVisible.value = true
}

async function withReason(title: string, placeholder: string) {
  const result = await ElMessageBox.prompt(placeholder, title, {
    inputType: 'textarea',
    inputValidator: (value) => Boolean(value.trim()) || '请说明原因',
  })
  return result.value.trim()
}

async function act(order: Order, action: 'confirm' | 'reject' | 'cancel' | 'deliver' | 'complete') {
  if (action === 'confirm') await adminApi.confirmOrder(order.id)
  if (action === 'reject')
    await adminApi.rejectOrder(order.id, await withReason('拒绝订单', '请告诉顾客无法接单的原因'))
  if (action === 'cancel')
    await adminApi.cancelOrder(order.id, await withReason('取消订单', '请填写取消原因'))
  if (action === 'deliver') await adminApi.deliverOrder(order.id)
  if (action === 'complete') await adminApi.completeOrder(order.id)
  ElMessage.success('订单状态已更新')
  detailVisible.value = false
  await loadOrders()
}

function statusTone(status: OrderStatusValue) {
  return status === 2 ? 'urgent' : status === 5 ? 'done' : status === 6 ? 'muted' : 'active'
}

watch(
  () => route.query.status,
  (value) => {
    query.status = value ? Number(value) : undefined
    loadOrders()
  },
)
onMounted(() => {
  if (route.query.status) query.status = Number(route.query.status)
  loadOrders()
})
</script>

<template>
  <div>
    <PageHeader
      eyebrow="ORDER WORKBENCH"
      title="订单工作台"
      description="把正在等待的订单放在最前面，减少来回寻找。"
    />
    <section class="paper-card filter-card">
      <el-tabs v-model="activeStatus">
        <el-tab-pane
          v-for="tab in statusTabs"
          :key="String(tab.value)"
          :name="tab.value === undefined ? 'all' : String(tab.value)"
          :label="tab.label"
        />
      </el-tabs>
      <div class="filter-row">
        <el-input v-model.trim="query.number" clearable placeholder="订单号" @keyup.enter="loadOrders" />
        <el-input v-model.trim="query.phone" clearable placeholder="顾客手机号" @keyup.enter="loadOrders" />
        <el-button type="primary" @click="searchOrders">查询</el-button>
      </div>
    </section>
    <section v-loading="loading" class="order-list">
      <article v-for="order in orders" :key="order.id" class="order-card" @click="openDetail(order)">
        <div class="order-card__status">
          <span :class="['status-dot', statusTone(order.status)]" />{{ orderStatusText[order.status] }}
        </div>
        <div class="order-card__main">
          <small>{{ order.orderTime }}</small
          ><strong># {{ order.number }}</strong>
          <p>{{ order.orderDishes || '打开查看商品明细' }}</p>
        </div>
        <div class="order-card__guest">
          <span>{{ order.consignee }}</span
          ><small>{{ order.phone }}</small>
          <p>{{ order.address }}</p>
        </div>
        <strong class="order-card__amount">¥{{ Number(order.amount).toFixed(2) }}</strong>
        <div class="order-card__actions" @click.stop>
          <el-button v-if="order.status === 2" type="primary" @click="act(order, 'confirm')">接单</el-button>
          <el-button v-if="order.status === 2" @click="act(order, 'reject')">拒绝</el-button>
          <el-button v-if="order.status === 3" type="primary" @click="act(order, 'deliver')"
            >开始配送</el-button
          >
          <el-button v-if="order.status === 4" type="primary" @click="act(order, 'complete')"
            >完成订单</el-button
          >
          <el-button text @click="openDetail(order)">详情</el-button>
        </div>
      </article>
      <EmptyState
        v-if="!loading && !orders.length"
        title="暂时没有这类订单"
        description="新的订单到来时，会第一时间出现在这里。"
      />
    </section>
    <el-pagination
      v-if="total"
      v-model:current-page="query.page"
      :page-size="query.pageSize"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="loadOrders"
    />

    <el-drawer v-model="detailVisible" title="订单详情" size="480px">
      <template v-if="selected">
        <div class="detail-status">
          <span :class="['status-dot', statusTone(selected.status)]" />{{ orderStatusText[selected.status] }}
        </div>
        <h3># {{ selected.number }}</h3>
        <p class="muted">{{ selected.orderTime }}</p>
        <div class="detail-lines">
          <div v-for="line in selected.orderDetailList" :key="line.id || line.name">
            <span
              >{{ line.name }}<small>{{ line.dishFlavor }}</small></span
            ><span>× {{ line.number }}</span
            ><strong>¥{{ Number(line.amount).toFixed(2) }}</strong>
          </div>
        </div>
        <dl class="detail-info">
          <div>
            <dt>顾客</dt>
            <dd>{{ selected.consignee }} · {{ selected.phone }}</dd>
          </div>
          <div>
            <dt>送达地址</dt>
            <dd>{{ selected.address }}</dd>
          </div>
          <div>
            <dt>备注</dt>
            <dd>{{ selected.remark || '无' }}</dd>
          </div>
        </dl>
        <div class="detail-total">
          <span>订单合计</span><strong>¥{{ Number(selected.amount).toFixed(2) }}</strong>
        </div>
        <div class="drawer-actions">
          <el-button v-if="selected.status === 2" @click="act(selected, 'reject')">拒绝订单</el-button
          ><el-button v-if="selected.status === 2" type="primary" @click="act(selected, 'confirm')"
            >确认接单</el-button
          >
          <el-button v-if="selected.status === 3" @click="act(selected, 'cancel')">取消订单</el-button
          ><el-button v-if="selected.status === 3" type="primary" @click="act(selected, 'deliver')"
            >开始配送</el-button
          >
          <el-button v-if="selected.status === 4" type="primary" @click="act(selected, 'complete')"
            >完成订单</el-button
          >
        </div>
      </template>
    </el-drawer>
  </div>
</template>
