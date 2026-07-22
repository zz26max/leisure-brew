<template>
  <section class="dashboard-card metrics-panel">
    <header class="dashboard-card__header">
      <div>
        <p class="dashboard-card__eyebrow">
          TODAY
        </p>
        <h2>今日经营</h2>
      </div>
      <div class="completion-rate">
        <span>订单完成率</span>
        <strong>{{ completionRate }}%</strong>
        <i><b :style="{ width: `${completionRate}%` }" /></i>
      </div>
      <router-link to="/statistics" class="dashboard-card__link">
        查看经营报告 <i class="el-icon-arrow-right" />
      </router-link>
    </header>

    <div class="metric-grid">
      <article v-for="metric in metrics" :key="metric.label" class="metric-card">
        <span>{{ metric.label }}</span>
        <strong>{{ metric.value }}</strong>
        <small>{{ metric.note }}</small>
      </article>
    </div>
  </section>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'

@Component({ name: 'TodayMetrics' })
export default class extends Vue {
  @Prop({ default: () => ({}) }) private overviewData!: any

  get completionRate() {
    const rate = Number(this.overviewData.orderCompletionRate || 0) * 100
    return Math.max(0, Math.min(100, Math.round(rate)))
  }

  get metrics() {
    return [
      {
        label: '营业额',
        value: this.formatMoney(this.overviewData.turnover),
        note: '今日实收',
      },
      {
        label: '有效订单',
        value: this.formatCount(this.overviewData.validOrderCount),
        note: '已支付订单',
      },
      {
        label: '平均客单价',
        value: this.formatMoney(this.overviewData.unitPrice),
        note: '每笔订单均价',
      },
      {
        label: '新增顾客',
        value: this.formatCount(this.overviewData.newUsers),
        note: '今天第一次来',
      },
    ]
  }

  private formatMoney(value: unknown) {
    return `¥ ${Number(value || 0).toFixed(2)}`
  }

  private formatCount(value: unknown) {
    return String(Number(value || 0))
  }
}
</script>

<style lang="scss" scoped>
.metrics-panel {
  margin-bottom: 20px;
}

.dashboard-card__header {
  display: flex;
  align-items: center;
}

.completion-rate {
  display: grid;
  grid-template-columns: auto auto;
  align-items: center;
  gap: 2px 12px;
  min-width: 190px;
  margin-left: 42px;

  span {
    color: $color-text-secondary;
    font-size: 12px;
  }

  strong {
    color: $color-primary;
    font-size: 18px;
    text-align: right;
  }

  i {
    grid-column: 1 / -1;
    height: 5px;
    overflow: hidden;
    background: #e9e3d8;
    border-radius: 99px;
  }

  b {
    display: block;
    height: 100%;
    background: $color-primary;
    border-radius: inherit;
  }
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 22px;
}

.metric-card {
  display: flex;
  min-width: 0;
  min-height: 126px;
  flex-direction: column;
  justify-content: center;
  padding: 19px 21px;
  background: #faf6ee;
  border: 1px solid $color-border-light;
  border-radius: $radius-md;

  span {
    color: $color-text-secondary;
    font-size: 12px;
  }

  strong {
    margin: 9px 0 7px;
    color: $color-text-primary;
    font-size: 27px;
    line-height: 1.2;
    font-variant-numeric: tabular-nums;
  }

  small {
    color: $color-text-muted;
    font-size: 11px;
  }
}
</style>
