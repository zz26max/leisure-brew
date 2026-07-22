<template>
  <div class="container">
    <h2 class="homeTitle">
      订单统计
    </h2>
    <div class="charBox">
      <div class="orderProportion">
        <div>
          <p>订单完成率</p>
          <p>{{ (orderdata.orderCompletionRate * 100).toFixed(1) }}%</p>
        </div>
        <div class="symbol">
          =
        </div>
        <div>
          <p>有效订单</p>
          <p>{{ orderdata.validOrderCount }}</p>
        </div>
        <div class="symbol">
          /
        </div>
        <div>
          <p>订单总数</p>
          <p>{{ orderdata.totalOrderCount }}</p>
        </div>
      </div>
      <div id="ordermain" style="width: 100%; height: 300px" />
      <ul class="orderListLine">
        <li class="one">
          <span />订单总数（个）
        </li>
        <li class="three">
          <span />有效订单（个）
        </li>
      </ul>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Watch } from 'vue-property-decorator'
import * as echarts from 'echarts'
import { CHART_COLORS } from '@/utils/chart-theme'
@Component({
  name: 'OrderStatistics',
})
export default class extends Vue {
  @Prop() private orderdata!: any
  @Prop() private overviewData!: any

  @Watch('orderdata')
  getData() {
    this.$nextTick(() => {
      this.initChart()
    })
  }
  initChart() {
    type EChartsOption = echarts.EChartsOption
    const chartDom = document.getElementById('ordermain') as any
    const myChart = echarts.init(chartDom)
    // // 循环遍历出x轴的数据
    // const baseDate = this.orderdata.list.map((item) => {
    //   return (item as any).date
    // })
    // const baseAmount = this.orderdata.list.map((item) => {
    //   return (item as any).amount
    // })
    // const baseValidNum = this.orderdata.list.map((item) => {
    //   return (item as any).accomplishNum
    // })
    // const baseAccomplishNum = this.orderdata.list.map((item) => {
    //   return (item as any).accomplishNum
    // })
    var option: any
    option = {
      // legend: {
      //   itemHeight: 3, //图例高
      //   itemWidth: 12, //图例宽
      //   icon: 'rect', //图例
      //   show: true,
      //   top: 'bottom',
      //   data: ['订单完成率', '有效订单', '订单总数'],
      // },
      tooltip: {
        trigger: 'axis',
        backgroundColor: '#fff',
        borderColor: '#E5E7EB',
        borderRadius: 8,
        textStyle: { color: '#1F2937', fontSize: 12 },
        extraCssText: 'box-shadow: 0 4px 12px rgba(0,0,0,0.08);',
      },
      grid: {
        top: '5%',
        left: '20',
        right: '50',
        bottom: '12%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        axisLabel: {
          color: CHART_COLORS.axisLabel,
          fontSize: 11,
        },
        axisLine: {
          lineStyle: {
            color: CHART_COLORS.gridLine,
            width: 1,
          },
        },
        data: this.orderdata.data.dateList,
      },
      yAxis: [
        {
          type: 'value',
          min: 0,
          interval: 50,
          axisLabel: {
            color: CHART_COLORS.axisLabel,
            fontSize: 11,
          },
        },
      ],
      series: [
        {
          name: '订单总数',
          type: 'line',
          smooth: true,
          showSymbol: false,
          symbolSize: 8,
          lineStyle: { color: CHART_COLORS.primary, width: 2 },
          itemStyle: { color: CHART_COLORS.primary },
          emphasis: {
            itemStyle: {
              color: '#fff',
              borderWidth: 3,
              borderColor: CHART_COLORS.primary,
            },
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(255, 122, 0, 0.2)' },
              { offset: 1, color: 'rgba(255, 122, 0, 0.02)' }
            ])
          },
          data: this.orderdata.data.orderCountList,
        },
        {
          name: '有效订单',
          type: 'line',
          smooth: true,
          showSymbol: false,
          symbolSize: 8,
          lineStyle: { color: CHART_COLORS.tertiary, width: 2 },
          itemStyle: { color: CHART_COLORS.tertiary },
          emphasis: {
            itemStyle: {
              color: '#fff',
              borderWidth: 3,
              borderColor: CHART_COLORS.tertiary,
            },
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(253, 127, 127, 0.2)' },
              { offset: 1, color: 'rgba(253, 127, 127, 0.02)' }
            ])
          },
          data: this.orderdata.data.validOrderCountList,
        }
      ],
    }
    option && myChart.setOption(option)
  }
}
</script>
