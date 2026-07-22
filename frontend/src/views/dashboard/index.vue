<template>
  <div class="dashboard-container home">
    <section class="store-welcome">
      <div>
        <p class="store-welcome__date">
          {{ todayLabel }}
        </p>
        <h2>{{ greeting }}，看看店里今天的节奏。</h2>
        <p class="store-welcome__copy">
          先处理正在等待的订单，其余事情慢慢来。
        </p>
      </div>
      <div class="store-welcome__actions">
        <el-button @click="$router.push('/order')">
          查看订单
        </el-button>
        <el-button type="primary" @click="$router.push('/drink/add')">
          上新饮品
        </el-button>
      </div>
    </section>

    <Orderview :orderview-data="orderviewData" />
    <Overview :overview-data="overviewData" />

    <div class="homeMain menu-overview">
      <CuisineStatistics :dishes-data="dishesData" />
      <SetMealStatistics :set-meal-data="setMealData" />
    </div>

    <OrderList
      :order-statics="orderStatics"
      @getOrderListBy3Status="getOrderListBy3Status"
    />
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import {
  getBusinessData,
  getOrderData,
  getOverviewDishes,
  getSetMealStatistics,
} from '@/api/index'
import { getOrderListBy } from '@/api/order'
import Overview from './components/overview.vue'
import Orderview from './components/orderview.vue'
import CuisineStatistics from './components/cuisineStatistics.vue'
import SetMealStatistics from './components/setMealStatistics.vue'
import OrderList from './components/orderList.vue'

@Component({
  name: 'Dashboard',
  components: {
    Overview,
    Orderview,
    CuisineStatistics,
    SetMealStatistics,
    OrderList,
  },
})
export default class extends Vue {
  private overviewData = {} as any
  private orderviewData = {} as any
  private dishesData = {} as any
  private setMealData = {} as any
  private orderStatics = {} as any

  get greeting() {
    const hour = new Date().getHours()
    if (hour < 11) return '早上好'
    if (hour < 14) return '中午好'
    if (hour < 18) return '下午好'
    return '晚上好'
  }

  get todayLabel() {
    return new Date().toLocaleDateString('zh-CN', {
      month: 'long',
      day: 'numeric',
      weekday: 'long',
    })
  }

  created() {
    this.loadDashboard()
  }

  private loadDashboard() {
    this.getBusinessData()
    this.getOrderStatisticsData()
    this.getMenuStatisticsData()
    this.getOrderListBy3Status()
  }

  private async getBusinessData() {
    const { data } = await getBusinessData()
    if (data && String(data.code) === '1') this.overviewData = data.data || {}
  }

  private async getOrderStatisticsData() {
    const { data } = await getOrderData()
    if (data && String(data.code) === '1') this.orderviewData = data.data || {}
  }

  private async getMenuStatisticsData() {
    const [dishes, setMeals] = await Promise.all([
      getOverviewDishes(),
      getSetMealStatistics(),
    ])
    this.dishesData = dishes.data.data || {}
    this.setMealData = setMeals.data.data || {}
  }

  private async getOrderListBy3Status() {
    const { data } = await getOrderListBy({})
    if (data && String(data.code) === '1') {
      this.orderStatics = data.data || {}
    }
  }
}
</script>

<style lang="scss" scoped>
.store-welcome {
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  min-height: 184px;
  margin-bottom: 20px;
  padding: 34px 38px;
  overflow: hidden;
  color: #fffaf0;
  background:
    radial-gradient(circle at 84% 24%, rgba(184, 101, 59, 0.42), transparent 22%),
    linear-gradient(120deg, $color-primary-dark, $color-primary 68%, #315c50);
  border-radius: $radius-xl;
  box-shadow: $shadow-md;

  &::after {
    position: absolute;
    top: -72px;
    right: -28px;
    width: 240px;
    height: 240px;
    border: 1px solid rgba(255, 250, 240, 0.12);
    border-radius: 50%;
    content: '';
    box-shadow:
      0 0 0 32px rgba(255, 250, 240, 0.03),
      0 0 0 64px rgba(255, 250, 240, 0.025);
  }

  > * {
    position: relative;
    z-index: 1;
  }

  &__date {
    margin: 0 0 9px;
    color: rgba(255, 250, 240, 0.66);
    font-size: 12px;
    letter-spacing: 0.12em;
  }

  h2 {
    margin: 0;
    font-family: 'Songti SC', STSong, SimSun, serif;
    font-size: 29px;
    font-weight: 700;
    letter-spacing: 0.04em;
  }

  &__copy {
    margin: 12px 0 0;
    color: rgba(255, 250, 240, 0.72);
    font-size: 13px;
  }

  &__actions {
    display: flex;
    gap: 10px;

    .el-button {
      color: #fffaf0;
      background: rgba(255, 250, 240, 0.08);
      border-color: rgba(255, 250, 240, 0.3);
    }

    .el-button--primary {
      color: $color-primary-dark !important;
      background: #fffaf0 !important;
      border-color: #fffaf0 !important;
    }
  }
}

.menu-overview {
  gap: 20px;
}
</style>
