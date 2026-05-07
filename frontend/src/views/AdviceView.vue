<template>
  <AppShell>
    <div class="stack">
      <PanelCard title="智能建议" subtitle="系统根据最近一段时间的排放数据，识别高排放风险并给出针对性建议。">
        <p v-if="errorMessage" class="feedback error">{{ errorMessage }}</p>
        <div v-else-if="loading" class="empty-state">正在分析近期排放数据...</div>
        <div v-else-if="adviceList.length" class="advice-grid">
          <article v-for="item in adviceList" :key="item.title" class="glass-card advice-card">
            <div class="chip">{{ item.activityType }}</div>
            <h3>{{ item.title }}</h3>
            <p class="muted">{{ item.description }}</p>
            <div class="metric-row">
              <strong>{{ item.metricLabel || '当前排放' }}</strong>
              <span>{{ formatMetric(item.actualEmission, item.metricUnit || 'kgCO2e') }}</span>
            </div>
            <p class="suggestion">{{ item.suggestion }}</p>
          </article>
        </div>
        <div v-else class="empty-state">目前还没有触发高排放提醒，继续保持现在的低碳节奏。</div>
      </PanelCard>
    </div>
  </AppShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'

import { adviceApi } from '../api/modules'
import AppShell from '../components/AppShell.vue'
import PanelCard from '../components/PanelCard.vue'

const loading = ref(true)
const errorMessage = ref('')
const adviceList = ref([])

onMounted(loadAdvice)

async function loadAdvice() {
  loading.value = true
  errorMessage.value = ''
  try {
    adviceList.value = await adviceApi.list()
  } catch (error) {
    errorMessage.value = error.message || '智能建议加载失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

function formatMetric(value, unit) {
  const number = Number(value || 0)
  if (unit === '条') return `${number.toFixed(0)} 条`
  if (unit === '%') return `${number.toFixed(0)}%`
  return `${number.toFixed(2)} ${unit}`
}
</script>

<style scoped>
.advice-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 18px;
}

.advice-card {
  padding: 24px;
}

.advice-card h3 {
  margin: 16px 0 10px;
}

.metric-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin: 14px 0;
}

.suggestion {
  margin: 18px 0 0;
  padding: 14px;
  border-radius: 16px;
  background: rgba(47, 143, 91, 0.08);
}

</style>
