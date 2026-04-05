<template>
  <AppShell>
    <div class="stack">
      <PanelCard title="记录列表" subtitle="按活动类型筛选、回看和编辑已录入的碳足迹数据。">
        <template #action>
          <RouterLink class="button-primary" to="/records/new">继续新增</RouterLink>
        </template>

        <div class="stack">
          <div class="field-grid">
            <div class="field">
              <label>活动类型筛选</label>
              <select v-model="filters.activityType">
                <option value="">全部</option>
                <option v-for="(config, key) in activityOptions" :key="key" :value="key">{{ config.label }}</option>
              </select>
            </div>
            <div class="field">
              <label>开始日期</label>
              <input v-model="filters.startDate" type="date" />
            </div>
            <div class="field">
              <label>结束日期</label>
              <input v-model="filters.endDate" type="date" />
            </div>
          </div>

          <div class="table-wrap" v-if="records.length">
            <table>
              <thead>
                <tr>
                  <th>类型</th>
                  <th>子类型</th>
                  <th>数值</th>
                  <th>排放</th>
                  <th>积分</th>
                  <th>时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in records" :key="item.id">
                  <td>{{ item.activityLabel }}</td>
                  <td>{{ item.subType }}</td>
                  <td>{{ item.amount }} {{ item.unit }}</td>
                  <td>{{ item.emissionKg }} kg</td>
                  <td>{{ item.points }}</td>
                  <td>{{ formatDate(item.occurredAt) }}</td>
                  <td>
                    <div class="action-row">
                      <button class="button-secondary" @click="editRecord(item.id)">编辑</button>
                      <button class="button-danger" @click="removeRecord(item.id)">删除</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else class="empty-state">还没有记录，先去新增一条碳足迹吧。</div>
        </div>
      </PanelCard>
    </div>
  </AppShell>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import { recordApi } from '../api/modules'
import AppShell from '../components/AppShell.vue'
import PanelCard from '../components/PanelCard.vue'
import { activityOptions } from '../config/activity-options'

const router = useRouter()
const records = ref([])
const filters = reactive({
  activityType: '',
  startDate: '',
  endDate: ''
})

watch(filters, fetchRecords, { deep: true })
onMounted(fetchRecords)

async function fetchRecords() {
  try {
    records.value = await recordApi.list({ ...filters })
  } catch (error) {
    alert(error.message)
  }
}

function editRecord(id) {
  router.push(`/records/new?id=${id}`)
}

async function removeRecord(id) {
  if (!window.confirm('确定删除这条记录吗？')) return
  try {
    await recordApi.remove(id)
    await fetchRecords()
  } catch (error) {
    alert(error.message)
  }
}

function formatDate(value) {
  return value?.replace('T', ' ').slice(0, 16)
}
</script>

<style scoped>
.action-row {
  display: flex;
  gap: 8px;
}
</style>
