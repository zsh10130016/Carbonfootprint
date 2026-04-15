<template>
  <PanelCard :title="title" :subtitle="subtitle">
    <div ref="chartRef" class="chart-box"></div>
  </PanelCard>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'

import echarts from '../lib/echarts'
import PanelCard from './PanelCard.vue'

const props = defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  option: { type: Object, required: true }
})

const chartRef = ref(null)
let chartInstance
let resizeObserver

function renderChart() {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  chartInstance.setOption(props.option, true)
}

function resizeChart() {
  chartInstance?.resize()
}

onMounted(() => {
  renderChart()
  window.addEventListener('resize', resizeChart)
  // Observe container changes so charts stay readable in responsive layouts.
  if (typeof ResizeObserver !== 'undefined' && chartRef.value) {
    resizeObserver = new ResizeObserver(() => resizeChart())
    resizeObserver.observe(chartRef.value)
  }
})

watch(() => props.option, renderChart, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  resizeObserver?.disconnect()
  chartInstance?.dispose()
})
</script>

<style scoped>
.chart-box {
  width: 100%;
  height: 320px;
}
</style>
