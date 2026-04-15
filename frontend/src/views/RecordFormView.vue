<template>
  <AppShell>
    <div class="stack">
      <PanelCard :title="panelTitle" :subtitle="panelSubtitle">
        <form class="stack" @submit.prevent="submit">
          <div v-if="prefillSource" class="chip">
            已从外部解析结果带入字段：{{ prefillSource }}
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
          <p class="helper-text">保存后系统会自动匹配排放因子，计算碳排放和积分。</p>
          <p v-if="message" :class="['feedback', isError ? 'error' : 'success']">{{ message }}</p>

          <div class="submit-row">
            <button class="button-primary" type="submit" :disabled="submitting || loadingDetail">
              {{ submitting ? '保存中...' : isEditing ? '更新记录' : '保存记录' }}
            </button>
            <button class="button-secondary" type="button" :disabled="submitting || loadingDetail" @click="fillLowCarbonExample">快速填充参考记录</button>
            <RouterLink class="button-secondary inline-link" to="/ocr">使用 OCR 识别</RouterLink>
          </div>
        </form>
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
const loadingDetail = ref(false)
const submitting = ref(false)
const message = ref('')
const isError = ref(false)
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
const panelTitle = computed(() => (isEditing.value ? '编辑碳足迹记录' : '新增碳足迹记录'))
const panelSubtitle = computed(() => (
  isEditing.value
    ? '修改已录入的出行、家庭用能或饮食消费数据。'
    : '通过动态表单记录出行、家庭用能和饮食消费。'
))

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
  loadingDetail.value = true
  message.value = ''
  isError.value = false
  try {
    const detail = await recordApi.detail(route.query.id)
    form.activityType = detail.activityType
    form.subType = detail.subType
    form.amount = String(detail.amount)
    form.unit = detail.unit
    form.note = detail.note || ''
    form.occurredAt = formatDateTimeLocal(detail.occurredAt)
  } catch (error) {
    isError.value = true
    message.value = error.message || '记录详情加载失败，请返回列表后重试。'
  } finally {
    loadingDetail.value = false
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
  const validationMessage = validateForm()
  if (validationMessage) {
    isError.value = true
    message.value = validationMessage
    return
  }

  submitting.value = true
  message.value = ''
  isError.value = false
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
    isError.value = true
    message.value = error.message || '记录保存失败，请稍后重试。'
  } finally {
    submitting.value = false
  }
}

function fillLowCarbonExample() {
  form.activityType = 'TRANSPORT'
  form.subType = 'SUBWAY'
  form.amount = '12'
  form.note = '地铁通勤'
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

function validateForm() {
  if (!form.activityType) return '请选择活动类型。'
  if (!form.subType) return '请选择子类型。'
  if (!form.occurredAt) return '请选择记录时间。'
  if (!form.amount || Number(form.amount) <= 0) return '请输入大于 0 的数值。'
  return ''
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
