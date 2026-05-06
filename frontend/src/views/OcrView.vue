<template>
  <AppShell>
    <div class="stack">
      <PanelCard title="OCR 智能识别" subtitle="上传交通票据或水电账单图片，系统会识别文字并提取可用于碳核算的字段。">
        <form class="stack" @submit.prevent="parse">
          <div class="field-grid">
            <div class="field">
              <label>票据类型</label>
              <select v-model="form.documentType">
                <option value="TRANSPORT_TICKET">交通票据</option>
                <option value="UTILITY_BILL">水电账单</option>
              </select>
            </div>
            <div class="field">
              <label>票据图片</label>
              <input accept="image/*" type="file" @change="handleFileChange" />
            </div>
          </div>

          <div v-if="previewUrl" class="preview-panel">
            <img :src="previewUrl" alt="待识别票据预览" />
            <div>
              <strong>{{ selectedFile?.name }}</strong>
              <p class="helper-text">{{ fileSizeText }}</p>
            </div>
          </div>

          <p class="helper-text">图片建议保持清晰、完整，尽量包含里程、用电量、用水量或用气量等关键字段。</p>
          <p v-if="message" :class="['feedback', isError ? 'error' : 'info']">{{ message }}</p>

          <div class="submit-row">
            <button class="button-primary" type="submit" :disabled="submitting">
              {{ submitting ? '识别中...' : '上传并识别' }}
            </button>
            <button class="button-secondary" type="button" :disabled="submitting || !selectedFile" @click="clearFile">重新选择</button>
          </div>
        </form>
      </PanelCard>

      <PanelCard v-if="result" title="识别结果" :subtitle="result.message">
        <div class="stack">
          <div class="field-grid">
            <div class="field">
              <label>识别提供方</label>
              <input :value="result.provider" readonly />
            </div>
            <div class="field">
              <label>推荐活动类型</label>
              <input :value="mappedActivityTypeLabel" readonly />
            </div>
            <div class="field">
              <label>推荐子类型</label>
              <input :value="mappedSubTypeLabel" readonly />
            </div>
            <div class="field">
              <label>推荐数值</label>
              <input :value="amountText" readonly />
            </div>
          </div>

          <div class="field">
            <label>识别到的文字</label>
            <pre class="result-box">{{ recognizedText }}</pre>
          </div>

          <p v-if="!canFillRecord" class="feedback error">暂未提取到完整数值，请换一张更清晰的图片，或到记录表单中手动补充。</p>

          <div class="submit-row">
            <button class="button-primary" :disabled="!canFillRecord" @click="goToRecordForm">带入记录表单</button>
            <RouterLink class="button-secondary inline-link" to="/records/new">手动新增记录</RouterLink>
          </div>
        </div>
      </PanelCard>
    </div>
  </AppShell>
</template>

<script setup>
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import { ocrApi } from '../api/modules'
import AppShell from '../components/AppShell.vue'
import PanelCard from '../components/PanelCard.vue'
import { activityOptions } from '../config/activity-options'

const router = useRouter()
const submitting = ref(false)
const result = ref(null)
const selectedFile = ref(null)
const previewUrl = ref('')
const message = ref('')
const isError = ref(false)
const form = reactive({
  documentType: 'TRANSPORT_TICKET'
})

const fields = computed(() => result.value?.fields || {})
const mappedActivityType = computed(() => fields.value.activityType || (
  form.documentType === 'UTILITY_BILL' ? 'HOME_ENERGY' : 'TRANSPORT'
))
const mappedActivityTypeLabel = computed(() => activityOptions[mappedActivityType.value]?.label || mappedActivityType.value)
const mappedSubTypeLabel = computed(() => (
  activityOptions[mappedActivityType.value]?.subTypes.find((item) => item.value === fields.value.subType)?.label || fields.value.subType || '待补充'
))
const amountText = computed(() => fields.value.amount ? `${fields.value.amount} ${fields.value.unit || ''}` : '待补充')
const canFillRecord = computed(() => Boolean(fields.value.activityType && fields.value.subType && fields.value.amount))
const recognizedText = computed(() => (result.value?.recognizedText || []).join('\n'))
const fileSizeText = computed(() => {
  if (!selectedFile.value) return ''
  return `${(selectedFile.value.size / 1024 / 1024).toFixed(2)} MB`
})

watch(
  () => form.documentType,
  () => {
    result.value = null
    message.value = ''
    isError.value = false
  }
)

onBeforeUnmount(() => {
  revokePreview()
})

async function parse() {
  if (!selectedFile.value) {
    isError.value = true
    message.value = '请先选择需要识别的票据图片。'
    return
  }

  const formData = new FormData()
  formData.append('documentType', form.documentType)
  formData.append('file', selectedFile.value)

  submitting.value = true
  message.value = ''
  isError.value = false
  try {
    result.value = await ocrApi.parse(formData)
    message.value = canFillRecord.value ? '识别完成，可带入记录表单。' : '识别完成，部分字段需要人工确认。'
  } catch (error) {
    isError.value = true
    message.value = error.message || '图片识别失败，请检查百度 OCR 配置或更换图片后重试。'
  } finally {
    submitting.value = false
  }
}

function handleFileChange(event) {
  const file = event.target.files?.[0]
  result.value = null
  message.value = ''
  isError.value = false
  selectedFile.value = file || null
  revokePreview()
  if (file) {
    previewUrl.value = URL.createObjectURL(file)
  }
}

function clearFile() {
  selectedFile.value = null
  result.value = null
  message.value = ''
  isError.value = false
  revokePreview()
}

function revokePreview() {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
}

function goToRecordForm() {
  if (!canFillRecord.value) return
  router.push({
    path: '/records/new',
    query: {
      source: '百度 OCR',
      activityType: fields.value.activityType,
      subType: fields.value.subType,
      amount: fields.value.amount,
      unit: fields.value.unit || '',
      note: `${mappedActivityTypeLabel.value}票据识别导入`
    }
  })
}
</script>

<style scoped>
.submit-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.preview-panel {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.64);
}

.preview-panel img {
  width: 132px;
  height: 92px;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid rgba(47, 143, 91, 0.16);
}

.result-box {
  margin: 0;
  max-height: 260px;
  padding: 14px;
  border-radius: 16px;
  background: rgba(47, 143, 91, 0.08);
  overflow: auto;
  white-space: pre-wrap;
}

.inline-link {
  display: inline-flex;
  align-items: center;
}

@media (max-width: 640px) {
  .preview-panel {
    align-items: flex-start;
    flex-direction: column;
  }

  .preview-panel img {
    width: 100%;
    height: 180px;
  }
}
</style>
