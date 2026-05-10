// Shared ECharts theme configuration for Leisure Brew
import * as echarts from 'echarts'

export const CHART_COLORS = {
  primary: '#FF7A00',
  primaryLight: '#FF9F43',
  secondary: '#FFB800',
  tertiary: '#FD7F7F',
  quaternary: '#FFA94D',
  quinary: '#FF6B6B',
  gridLine: '#F0F0F0',
  axisLabel: '#9CA3AF',
}

// Shared gradient fill for area charts
export function primaryAreaGradient(): object {
  return {
    type: 'linear',
    x: 0, y: 0, x2: 0, y2: 1,
    colorStops: [
      { offset: 0, color: 'rgba(255, 122, 0, 0.25)' },
      { offset: 1, color: 'rgba(255, 122, 0, 0.02)' }
    ]
  }
}

export function secondaryAreaGradient(): object {
  return {
    type: 'linear',
    x: 0, y: 0, x2: 0, y2: 1,
    colorStops: [
      { offset: 0, color: 'rgba(253, 127, 127, 0.2)' },
      { offset: 1, color: 'rgba(253, 127, 127, 0.02)' }
    ]
  }
}

// Shared grid config
export function baseGrid(): object {
  return {
    top: '5%',
    left: '20',
    right: '50',
    bottom: '12%',
    containLabel: true,
  }
}

// Shared xAxis config
export function baseXAxis(data: string[]): object {
  return {
    type: 'category',
    boundaryGap: false,
    axisLabel: {
      color: CHART_COLORS.axisLabel,
      fontSize: 11,
    },
    axisLine: {
      lineStyle: { color: CHART_COLORS.gridLine }
    },
    data,
  }
}

// Shared tooltip config
export function baseTooltip(): object {
  return {
    trigger: 'axis',
    backgroundColor: '#fff',
    borderColor: '#E5E7EB',
    borderRadius: 8,
    textStyle: { color: '#1F2937', fontSize: 12 },
    extraCssText: 'box-shadow: 0 4px 12px rgba(0,0,0,0.08);',
  }
}
