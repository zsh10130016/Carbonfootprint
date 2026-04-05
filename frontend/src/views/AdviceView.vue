<template>
  <AppShell>
    <div class="stack">
      <PanelCard title="智能建议" subtitle="系统根据最近一段时间的排放数据，识别高排放风险并给出针对性建议。">
        <div v-if="adviceList.length" class="advice-grid">
          <article v-for="item in adviceList" :key="item.title" class="glass-card advice-card">
            <div class="chip">{{ item.activityType }}</div>
            <h3>{{ item.title }}</h3>
            <p class="muted">{{ item.description }}</p>
            <div class="metric-row">
              <strong>当前排放</strong>
              <span>{{ Number(item.actualEmission).toFixed(2) }} kg</span>
            </div>
            <div class="metric-row">
              <strong>阈值</strong>
              <span>{{ Number(item.threshold).toFixed(2) }} kg / {{ item.periodDays }} 天</span>
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

const adviceList = ref([])

onMounted(async () => {
  try {
    adviceList.value = await adviceApi.list()
  } catch (error) {
    alert(error.message)
  }
})
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
