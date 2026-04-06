<template>
  <AppShell>
    <div class="stack">
      <PanelCard title="新增碳足迹记录" subtitle="通过动态表单记录出行、家庭用能和饮食消费。">
        <div class="stack">
          <div v-if="prefillSource" class="chip">
            已从 OCR 识别结果带入字段：{{ prefillSource }}
          </div>

          <div class="field-grid">
            <div class="field">
              <label>活动类型</label>
              <select v-model="form.activityType">
                <option v-for="(config, key) in activityOptions" :key="key" :value="key">{{ config.label }}</option>
              </select>
            </div>
            <div class="field">
              <label>子类型</label>
              <select v-model="form.subType">
                <option v-for="item in currentSubTypes" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </div>
            <div class="field">
              <label>数值</label>
              <input v-model="form.amount" type="number" min="0" step="0.01" />
            </div>
            <div class="field">
              <label>单位</label>
              <input v-model="form.unit" readonly />
            </div>
            <div class="field">
              <label>记录时间</label>
              <input v-model="form.occurredAt" type="datetime-local" />
            </div>
          </div>

          <div class="field">
            <label>备注</label>
            <textarea
              v-model="form.note"
              rows="4"
              placeholder="比如：晚高峰打车回宿舍 / 4 月电费账单等"
            />
          </div>

          <div class="submit-row">
            <button class="button-primary" :disabled="submitting" @click="submit">
              {{ submitting ? '保存中...' : isEditing ? '更新记录' : '保存记录' }}
            </button>
            <button class="button-secondary" type="button" @click="fillLowCarbonExample">填入低碳示例</button>
            <RouterLink class="button-secondary inline-link" to="/ocr">使用 OCR 识别</RouterLink>
          </div>
        </div>
      </PanelCard>
    </div>
  </AppShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { recordApi } from '../api/modules'
import AppShell from '../components/AppShell.vue'
import PanelCard from '../components/PanelCard.vue'
import { activityOptions } from '../config/activity-options'

const route = useRoute()
const router = useRouter()
const submitting = ref(false)
const form = reactive({
  activityType: 'TRANSPORT',
  subType: 'BUS',
  amount: '10',
  unit: 'km',
  note: '',
  occurredAt: formatDateTimeLocal(new Date())
})

const isEditing = computed(() => Boolean(route.query.id))
const currentSubTypes = computed(() => activityOptions[form.activityType].subTypes)
const prefillSource = computed(() => route.query.source || '')

// Keep unit aligned with the selected subtype so the backend can match the factor table.
watch(
  () => [form.activityType, form.subType],
  () => {
    const selected = currentSubTypes.value.find((item) => item.value === form.subType) || currentSubTypes.value[0]
    form.subType = selected.value
    form.unit = selected.unit || activityOptions[form.activityType].unit
  },
  { immediate: true }
)

onMounted(async () => {
  if (route.query.id) {
    await loadDetail()
    return
  }
  applyPrefillFromQuery()
})

async function loadDetail() {
  try {
    const detail = await recordApi.detail(route.query.id)
    form.activityType = detail.activityType
    form.subType = detail.subType
    form.amount = String(detail.amount)
    form.unit = detail.unit
    form.note = detail.note || ''
    form.occurredAt = formatDateTimeLocal(detail.occurredAt)
  } catch (error) {
    alert(error.message)
  }
}

function applyPrefillFromQuery() {
  if (typeof route.query.activityType === 'string' && activityOptions[route.query.activityType]) {
    form.activityType = route.query.activityType
  }
  if (typeof route.query.subType === 'string') {
    form.subType = route.query.subType
  }
  if (typeof route.query.amount === 'string') {
    form.amount = route.query.amount
  }
  if (typeof route.query.note === 'string') {
    form.note = route.query.note
  }
}

async function submit() {
  submitting.value = true
  try {
    const payload = {
      ...form,
      amount: Number(form.amount),
      // datetime-local is already a local time, so send it directly instead of converting to UTC.
      occurredAt: normalizeLocalDateTime(form.occurredAt)
    }
    if (isEditing.value) {
      await recordApi.update(route.query.id, payload)
    } else {
      await recordApi.create(payload)
    }
    router.push('/records')
  } catch (error) {
    alert(error.message)
  } finally {
    submitting.value = false
  }
}

function fillLowCarbonExample() {
  form.activityType = 'TRANSPORT'
  form.subType = 'SUBWAY'
  form.amount = '12'
  form.note = '地铁通勤示例'
}

function formatDateTimeLocal(value) {
  const date = new Date(value)
  const pad = (num) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function normalizeLocalDateTime(value) {
  if (!value) return value
  return value.length === 16 ? `${value}:00` : value.slice(0, 19)
}
</script>

<style scoped>
.submit-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.inline-link {
  display: inline-flex;
  align-items: center;
}
</style>
