<template>
  <div class="container">
    <h2 class="homeTitle">
      营业额统计
    </h2>
    <div class="charBox">
      <div id="main" style="width: 100%; height: 320px" />
      <ul class="orderListLine turnover">
        <li>营业额(元)</li>
      </ul>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Watch } from 'vue-property-decorator'
import * as echarts from 'echarts'
import { CHART_COLORS, primaryAreaGradient } from '@/utils/chart-theme'
@Component({
  name: 'TurnoverStatistics',
})
export default class extends Vue {
  @Prop() private turnoverdata!: any
  @Watch('turnoverdata')
  getData() {
    this.$nextTick(() => {
      this.initChart()
    })
  }
  initChart() {
    type EChartsOption = echarts.EChartsOption
    const chartDom = document.getElementById('main') as any
    const myChart = echarts.init(chartDom)

    var option: any
    option = {
      // title: {
      //   text: '营业额(元)',
      //   top: 'bottom',
      //   left: 'center',
      //   textAlign: 'center',
      //   textStyle: {
      //     fontSize: 12,
      //     fontWeight: 'normal',
      //   },
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
        left: '10',
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
        data: this.turnoverdata.dateList,
      },
      yAxis: [
        {
          type: 'value',
          min: 0,
          axisLabel: {
            color: CHART_COLORS.axisLabel,
            fontSize: 11,
          }
        }
      ],
      series: [
        {
          name: '营业额',
          type: 'line',
          smooth: true,
          showSymbol: false,
          symbolSize: 8,
          lineStyle: {
            color: CHART_COLORS.primary,
            width: 2,
          },
          itemStyle: {
            color: CHART_COLORS.primary,
          },
          emphasis: {
            itemStyle: {
              color: '#fff',
              borderWidth: 3,
              borderColor: CHART_COLORS.primary,
            },
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(255, 122, 0, 0.25)' },
              { offset: 1, color: 'rgba(255, 122, 0, 0.02)' }
            ])
          },
          data: this.turnoverdata.turnoverList,
        },
      ],
    }
    option && myChart.setOption(option)
  }
}
</script>
