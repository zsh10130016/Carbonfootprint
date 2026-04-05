<template>
  <AppShell>
    <div class="stack">
      <PanelCard title="统计分析" subtitle="把趋势、结构和分类结果放在同一页，适合答辩时完整展示分析能力。">
        <div class="stats-headline">
          <span class="chip">趋势分析</span>
          <span class="chip">来源占比</span>
          <span class="chip">分类对比</span>
        </div>
      </PanelCard>

      <div class="stats-layout">
        <ChartPanel title="近 7 天趋势" subtitle="每日排放量变化" :option="trendOption" />
        <ChartPanel title="来源占比" subtitle="哪类活动贡献最高" :option="ratioOption" />
      </div>

      <ChartPanel title="分类统计" subtitle="不同子类型的排放与积分对比" :option="categoryOption" />
    </div>
  </AppShell>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

import { dashboardApi } from '../api/modules'
import AppShell from '../components/AppShell.vue'
import ChartPanel from '../components/ChartPanel.vue'
import PanelCard from '../components/PanelCard.vue'

const trend = ref([])
const ratio = ref([])
const categories = ref([])

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: trend.value.map((item) => item.date.slice(5)) },
  yAxis: { type: 'value', name: 'kgCO2e' },
  series: [{ type: 'bar', data: trend.value.map((item) => item.emissionKg), itemStyle: { color: '#2f8f5b' } }]
}))

const ratioOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{ type: 'pie', radius: '68%', data: ratio.value }]
}))

const categoryOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['排放(kg)', '积分'] },
  xAxis: { type: 'category', data: categories.value.map((item) => item.name) },
  yAxis: [{ type: 'value', name: '排放' }, { type: 'value', name: '积分' }],
  series: [
    { name: '排放(kg)', type: 'bar', data: categories.value.map((item) => item.emissionKg), itemStyle: { color: '#d88d3b' } },
    { name: '积分', type: 'line', yAxisIndex: 1, data: categories.value.map((item) => item.points), lineStyle: { color: '#1f6d44', width: 3 } }
  ]
}))

onMounted(async () => {
  try {
    ;[trend.value, ratio.value, categories.value] = await Promise.all([
      dashboardApi.trend(),
      dashboardApi.sourceRatio(),
      dashboardApi.categories()
    ])
  } catch (error) {
    alert(error.message)
  }
})
</script>

<style scoped>
.stats-layout {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.stats-headline {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 960px) {
  .stats-layout {
    grid-template-columns: 1fr;
  }
}
</style>
