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

function renderChart() {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  chartInstance.setOption(props.option, true)
}

onMounted(() => {
  renderChart()
  window.addEventListener('resize', renderChart)
})

watch(() => props.option, renderChart, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', renderChart)
  chartInstance?.dispose()
})
</script>

<style scoped>
.chart-box {
  width: 100%;
  height: 320px;
}
</style>
