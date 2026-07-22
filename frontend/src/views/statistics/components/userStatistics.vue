<template>
  <div class="container">
    <h2 class="homeTitle">
      用户统计
    </h2>
    <div class="charBox">
      <div id="usermain" style="width: 100%; height: 320px" />
      <ul class="orderListLine user">
        <li class="one">
          <span />用户总量（个）
        </li>
        <li class="three">
          <span />新增用户（个）
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
  name: 'UserStatistics',
})
export default class extends Vue {
  @Prop() private userdata!: any
  @Watch('userdata')
  getData() {
    this.$nextTick(() => {
      this.initChart()
    })
  }
  initChart() {
    type EChartsOption = echarts.EChartsOption
    const chartDom = document.getElementById('usermain') as any
    const myChart = echarts.init(chartDom)
    var option: any
    option = {
      // legend: {
      //   itemHeight: 3, //图例高
      //   itemWidth: 12, //图例宽
      //   icon: 'rect', //图例
      //   show: true,
      //   top: 'bottom',
      //   data: ['用户总量', '新增用户'],
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
        data: this.userdata.dateList,
      },
      yAxis: [
        {
          type: 'value',
          min: 0,
          axisLabel: {
            color: CHART_COLORS.axisLabel,
            fontSize: 11,
          },
        },
      ],
      series: [
        {
          name: '用户总量',
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
          data: this.userdata.totalUserList,
        },
        {
          name: '新增用户',
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
          data: this.userdata.newUserList,
        },
      ],
    }
    option && myChart.setOption(option)
  }
}
</script>
<style scoped>
</style>
