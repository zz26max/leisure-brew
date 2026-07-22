<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import PageHeader from '@/components/PageHeader.vue'
import { adminApi } from '@/services/admin-api'

echarts.use([LineChart, BarChart, GridComponent, TooltipComponent, CanvasRenderer])
const range = ref<[Date, Date]>([new Date(Date.now() - 6 * 86400000), new Date()])
const turnoverChart = ref<HTMLElement>()
const rankingChart = ref<HTMLElement>()
let turnoverInstance: echarts.ECharts | undefined
let rankingInstance: echarts.ECharts | undefined
const totalTurnover = ref(0)
const bestSeller = ref('暂无')
const formatDate = (date: Date) => date.toISOString().slice(0, 10)
const rangeText = computed(() => `${formatDate(range.value[0])} — ${formatDate(range.value[1])}`)

async function load() {
  const begin = formatDate(range.value[0])
  const end = formatDate(range.value[1])
  const [turnoverResponse, topResponse] = await Promise.all([
    adminApi.turnover(begin, end),
    adminApi.top10(begin, end),
  ])
  const dates = turnoverResponse.data.data.dateList.split(',')
  const turnover = turnoverResponse.data.data.turnoverList.split(',').map(Number)
  const names = topResponse.data.data.nameList ? topResponse.data.data.nameList.split(',') : []
  const numbers = topResponse.data.data.numberList
    ? topResponse.data.data.numberList.split(',').map(Number)
    : []
  totalTurnover.value = turnover.reduce((sum, value) => sum + value, 0)
  bestSeller.value = names[0] || '暂无'
  turnoverInstance?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 44, right: 20, top: 30, bottom: 32 },
    xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: '#d8d4ca' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#eeeae1' } } },
    series: [
      {
        type: 'line',
        data: turnover,
        smooth: true,
        symbolSize: 7,
        lineStyle: { color: '#286454', width: 3 },
        itemStyle: { color: '#e8c96a' },
        areaStyle: { color: 'rgba(40,100,84,.08)' },
      },
    ],
  })
  rankingInstance?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 96, right: 24, top: 20, bottom: 28 },
    xAxis: { type: 'value', splitLine: { show: false } },
    yAxis: {
      type: 'category',
      data: names.slice(0, 6).reverse(),
      axisLine: { show: false },
      axisTick: { show: false },
    },
    series: [
      {
        type: 'bar',
        data: numbers.slice(0, 6).reverse(),
        barWidth: 16,
        itemStyle: { color: '#286454', borderRadius: [0, 8, 8, 0] },
      },
    ],
  })
}
function resize() {
  turnoverInstance?.resize()
  rankingInstance?.resize()
}
onMounted(() => {
  turnoverInstance = echarts.init(turnoverChart.value)
  rankingInstance = echarts.init(rankingChart.value)
  load()
  window.addEventListener('resize', resize)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  turnoverInstance?.dispose()
  rankingInstance?.dispose()
})
</script>

<template>
  <div>
    <PageHeader eyebrow="BUSINESS NOTES" title="经营分析" description="用趋势帮助判断，不让数字替你做决定。"
      ><el-date-picker
        v-model="range"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        @change="load"
    /></PageHeader>
    <section class="report-summary">
      <article>
        <span>所选周期营业额</span><strong>¥{{ totalTurnover.toFixed(2) }}</strong
        ><small>{{ rangeText }}</small>
      </article>
      <article>
        <span>最受欢迎</span><strong>{{ bestSeller }}</strong
        ><small>按售出杯数统计</small>
      </article>
      <article class="report-summary__note">
        <span>看数提示</span>
        <p>把天气、节日和上新一起考虑，趋势才更有意义。</p>
      </article>
    </section>
    <section class="report-grid">
      <article class="paper-card">
        <div class="card-heading">
          <div>
            <p class="eyebrow">TURNOVER</p>
            <h3>营业额趋势</h3>
          </div>
        </div>
        <div ref="turnoverChart" class="chart" />
      </article>
      <article class="paper-card">
        <div class="card-heading">
          <div>
            <p class="eyebrow">FAVORITES</p>
            <h3>本期人气饮品</h3>
          </div>
        </div>
        <div ref="rankingChart" class="chart" />
      </article>
    </section>
  </div>
</template>
