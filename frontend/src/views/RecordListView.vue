<template>
  <AppShell>
    <div class="stack">
      <PanelCard :title="panelTitle" subtitle="按活动类型和日期范围筛选、回看和编辑已录入的碳足迹数据。">
        <template #action>
          <div class="action-row">
            <button class="button-secondary" type="button" @click="resetFilters">清空筛选</button>
            <RouterLink class="button-primary" to="/records/new">继续新增</RouterLink>
          </div>
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
          <p v-if="message" :class="['feedback', isError ? 'error' : 'info']">{{ message }}</p>

          <div v-if="loading" class="empty-state">正在加载记录列表...</div>
          <div class="table-wrap" v-else-if="records.length">
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
                  <td>{{ formatSubType(item.activityType, item.subType) }}</td>
                  <td>{{ item.amount }} {{ item.unit }}</td>
                  <td>{{ item.emissionKg }} kg</td>
                  <td>{{ item.points }}</td>
                  <td>{{ formatDate(item.occurredAt) }}</td>
                  <td>
                    <div class="action-row">
                      <button class="button-secondary" @click="editRecord(item.id)">编辑</button>
                      <button class="button-danger" :disabled="deletingId === item.id" @click="removeRecord(item.id)">
                        {{ deletingId === item.id ? '删除中...' : '删除' }}
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else class="empty-state">还没有符合条件的记录，先去新增一条碳足迹吧。</div>
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
const loading = ref(false)
const deletingId = ref(null)
const message = ref('')
const isError = ref(false)
const records = ref([])
const filters = reactive({
  activityType: '',
  startDate: '',
  endDate: ''
})
const panelTitle = ref('记录列表')

watch(filters, () => fetchRecords(), { deep: true })
onMounted(() => fetchRecords())

async function fetchRecords(successMessage = '') {
  if (filters.startDate && filters.endDate && filters.startDate > filters.endDate) {
    isError.value = true
    message.value = '结束日期不能早于开始日期。'
    records.value = []
    panelTitle.value = '记录列表'
    return
  }

  loading.value = true
  message.value = ''
  isError.value = false
  try {
    records.value = await recordApi.list({ ...filters })
    panelTitle.value = `记录列表 (${records.value.length})`
    if (successMessage) {
      message.value = successMessage
    } else if (!records.value.length) {
      message.value = '当前筛选条件下暂无记录。'
    }
  } catch (error) {
    isError.value = true
    message.value = error.message || '记录列表加载失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

function editRecord(id) {
  router.push(`/records/new?id=${id}`)
}

async function removeRecord(id) {
  if (!window.confirm('确定删除这条记录吗？')) return
  deletingId.value = id
  try {
    await recordApi.remove(id)
    await fetchRecords('记录已删除。')
  } catch (error) {
    isError.value = true
    message.value = error.message || '记录删除失败，请稍后重试。'
  } finally {
    deletingId.value = null
  }
}

function formatDate(value) {
  return value?.replace('T', ' ').slice(0, 16)
}

function formatSubType(activityType, subType) {
  return activityOptions[activityType]?.subTypes.find((item) => item.value === subType)?.label || subType
}

function resetFilters() {
  filters.activityType = ''
  filters.startDate = ''
  filters.endDate = ''
}
</script>

<style scoped>
.action-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
