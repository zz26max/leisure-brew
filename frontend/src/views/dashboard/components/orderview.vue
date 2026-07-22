<template>
  <section class="dashboard-card order-queue">
    <header class="dashboard-card__header">
      <div>
        <p class="dashboard-card__eyebrow">
          TO DO
        </p>
        <h2>待办订单</h2>
      </div>
      <p class="dashboard-card__hint">
        先照顾正在等待的顾客
      </p>
      <router-link to="/order" class="dashboard-card__link">
        全部订单 <i class="el-icon-arrow-right" />
      </router-link>
    </header>

    <div class="queue-grid">
      <router-link to="/order?status=2" class="queue-card is-urgent">
        <span><i class="iconfont icon-waiting" /> 待接单</span>
        <strong>{{ count(orderviewData.waitingOrders) }}</strong>
        <small>需要尽快确认</small>
      </router-link>
      <router-link to="/order?status=3" class="queue-card">
        <span><i class="iconfont icon-staySway" /> 待派送</span>
        <strong>{{ count(orderviewData.deliveredOrders) }}</strong>
        <small>等待安排配送</small>
      </router-link>
      <router-link to="/order?status=5" class="queue-card is-complete">
        <span><i class="iconfont icon-complete" /> 今日完成</span>
        <strong>{{ count(orderviewData.completedOrders) }}</strong>
        <small>已经认真送达</small>
      </router-link>
    </div>

    <footer class="queue-summary">
      <span>今日全部 <strong>{{ count(orderviewData.allOrders) }}</strong></span>
      <span>已取消 <strong>{{ count(orderviewData.cancelledOrders) }}</strong></span>
    </footer>
  </section>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'

@Component({ name: 'OrderQueue' })
export default class extends Vue {
  @Prop({ default: () => ({}) }) private orderviewData!: any

  private count(value: unknown) {
    return Number(value || 0)
  }
}
</script>

<style lang="scss" scoped>
.order-queue {
  margin-bottom: 20px;
}

.queue-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 22px;
}

.queue-card {
  position: relative;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  min-height: 126px;
  padding: 20px 22px;
  color: $color-text-primary;
  background: #faf6ee;
  border: 1px solid $color-border-light;
  border-radius: $radius-md;

  span {
    color: $color-text-secondary;
    font-size: 13px;
  }

  i {
    margin-right: 5px;
    color: $color-warning;
    font-size: 18px;
    vertical-align: -1px;
  }

  strong {
    grid-row: 1 / 3;
    grid-column: 2;
    align-self: center;
    color: $color-text-primary;
    font-size: 34px;
    font-variant-numeric: tabular-nums;
  }

  small {
    color: $color-text-muted;
    font-size: 11px;
  }

  &:hover {
    border-color: #c9c3b8;
    box-shadow: $shadow-sm;
  }

  &.is-urgent {
    background: rgba(183, 121, 47, 0.08);
    border-color: rgba(183, 121, 47, 0.24);
  }

  &.is-complete i {
    color: $color-success;
  }
}

.queue-summary {
  display: flex;
  gap: 26px;
  margin-top: 17px;
  color: $color-text-secondary;
  font-size: 12px;

  strong {
    margin-left: 4px;
    color: $color-text-primary;
    font-size: 13px;
  }
}
</style>
