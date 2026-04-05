<template>
  <AppShell>
    <div class="stack">
      <section class="hero-banner glass-card">
        <div>
          <h1 class="banner-title">欢迎回来，{{ authStore.user?.fullName || authStore.user?.username }}</h1>
          <p class="section-subtitle">
            这里会持续追踪你的碳排放节奏、主要来源和本周变化，适合答辩演示时先讲“系统的价值和主界面”。
          </p>
        </div>
        <div class="chip-group">
          <span class="chip">本周碳排放 {{ formatNumber(summary.weekEmission) }} kg</span>
          <span class="chip">累计积分 {{ summary.totalPoints }}</span>
        </div>
      </section>

      <section class="stats-grid">
        <StatCard label="累计排放" :value="`${formatNumber(summary.totalEmission)} kg`" tip="系统自动累计所有记录" />
        <StatCard label="本周排放" :value="`${formatNumber(summary.weekEmission)} kg`" tip="近 7 天统计" />
        <StatCard label="积分" :value="summary.totalPoints" tip="低碳行为会沉淀成排行积分" />
        <StatCard label="主要来源" :value="summary.topSource || '暂无'" tip="按排放量最高的类别识别" />
      </section>

      <div class="dashboard-grid">
        <ChartPanel title="本周碳排放趋势" subtitle="近 7 天每日排放变化" :option="trendOption" />
        <ChartPanel title="排放来源占比" subtitle="帮助识别当前的主要排放来源" :option="ratioOption" />
      </div>
    </div>
  </AppShell>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'

import { dashboardApi } from '../api/modules'
import AppShell from '../components/AppShell.vue'
import ChartPanel from '../components/ChartPanel.vue'
import StatCard from '../components/StatCard.vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const summary = ref({
  totalEmission: 0,
  weekEmission: 0,
  totalPoints: 0,
  totalRecords: 0,
  topSource: '暂无数据'
})
const trend = ref([])
const ratio = ref([])

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: {
    type: 'category',
    data: trend.value.map((item) => item.date.slice(5))
  },
  yAxis: { type: 'value', name: 'kgCO2e' },
  series: [
    {
      data: trend.value.map((item) => item.emissionKg),
      type: 'line',
      smooth: true,
      areaStyle: { color: 'rgba(47, 143, 91, 0.18)' },
      lineStyle: { color: '#2f8f5b', width: 3 }
    }
  ]
}))

const ratioOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'pie',
      radius: ['42%', '72%'],
      itemStyle: { borderRadius: 16, borderColor: '#fff', borderWidth: 4 },
      data: ratio.value
    }
  ]
}))

onMounted(async () => {
  try {
    ;[summary.value, trend.value, ratio.value] = await Promise.all([
      dashboardApi.summary(),
      dashboardApi.trend(),
      dashboardApi.sourceRatio()
    ])
  } catch (error) {
    alert(error.message)
  }
})

function formatNumber(value) {
  return Number(value || 0).toFixed(2)
}
</script>

<style scoped>
.hero-banner {
  padding: 26px;
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: center;
}

.banner-title {
  margin: 0 0 10px;
  font-size: clamp(1.8rem, 4vw, 2.8rem);
}

.chip-group {
  display: grid;
  gap: 10px;
}

.stats-grid,
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.stats-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

@media (max-width: 960px) {
  .hero-banner,
  .stats-grid,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
