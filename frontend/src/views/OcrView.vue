<template>
  <AppShell>
    <div class="stack">
      <PanelCard title="OCR 智能识别" subtitle="上传交通票据或水电账单图片，系统会先调用百度 OCR 识别文字，再由 DeepSeek 分析并提取字段。">
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
              <label>图片文件</label>
              <input ref="fileInput" type="file" accept="image/jpeg,image/png,image/bmp" @change="handleFileChange" />
            </div>
          </div>

          <div v-if="selectedFile" class="upload-preview">
            <img v-if="previewUrl" :src="previewUrl" :alt="selectedFile.name" />
            <div>
              <strong>{{ selectedFile.name }}</strong>
              <p class="helper-text">{{ formatFileSize(selectedFile.size) }}，{{ selectedFile.type || '未知格式' }}</p>
              <button class="button-secondary" type="button" @click="clearFile">重新选择图片</button>
            </div>
          </div>

          <p class="helper-text">支持 JPG、PNG 或 BMP。识别结果会先展示在本页，确认后再带入记录表单。</p>
          <p v-if="message" :class="['feedback', isError ? 'error' : 'info']">{{ message }}</p>

          <div class="submit-row">
            <button class="button-primary" type="submit" :disabled="submitting || !selectedFile">
              {{ submitting ? '正在识别并分析...' : '开始识别图片' }}
            </button>
          </div>
        </form>
      </PanelCard>

      <PanelCard v-if="result" title="识别结果" :subtitle="result.message">
        <div class="stack">
          <div class="field-grid">
            <div class="field">
              <label>推荐活动类型</label>
              <input :value="activityTypeLabel" readonly />
            </div>
            <div class="field">
              <label>推荐子类型</label>
              <input :value="subTypeLabel" readonly />
            </div>
            <div class="field">
              <label>推荐数值</label>
              <input :value="amountLabel" readonly />
            </div>
          </div>

          <div class="submit-row">
            <button class="button-primary" :disabled="!canFillRecord" @click="goToRecordForm">带入记录表单</button>
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

const MAX_FILE_SIZE = 8 * 1024 * 1024
const SUPPORTED_FILE_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/bmp']

const router = useRouter()
const submitting = ref(false)
const result = ref(null)
const message = ref('')
const isError = ref(false)
const fileInput = ref(null)
const selectedFile = ref(null)
const previewUrl = ref('')
const form = reactive({ documentType: 'TRANSPORT_TICKET' })

const fields = computed(() => result.value?.fields || {})
const activityTypeLabel = computed(() => activityOptions[fields.value.activityType]?.label || '未识别')
const subTypeLabel = computed(() => {
  const options = activityOptions[fields.value.activityType]?.subTypes || []
  return options.find((item) => item.value === fields.value.subType)?.label || fields.value.subType || '未识别'
})
const amountLabel = computed(() => (
  fields.value.amount ? `${fields.value.amount} ${fields.value.unit || ''}` : '未识别到，带入后可手动补充'
))
const canFillRecord = computed(() => Boolean(fields.value.activityType && fields.value.subType))

watch(
  () => form.documentType,
  () => {
    result.value = null
    message.value = ''
    isError.value = false
  }
)

onBeforeUnmount(() => revokePreviewUrl())

function handleFileChange(event) {
  const file = event.target.files?.[0]
  if (!file) return
  result.value = null
  message.value = ''
  isError.value = false

  const fileType = (file.type || '').toLowerCase()
  if (!SUPPORTED_FILE_TYPES.includes(fileType)) {
    selectedFile.value = null
    revokePreviewUrl()
    isError.value = true
    message.value = '仅支持 JPG、PNG 或 BMP 格式的票据图片。'
    return
  }
  if (file.size > MAX_FILE_SIZE) {
    selectedFile.value = null
    revokePreviewUrl()
    isError.value = true
    message.value = '图片大小不能超过 8MB。'
    return
  }

  selectedFile.value = file
  revokePreviewUrl()
  previewUrl.value = URL.createObjectURL(file)
}

function clearFile() {
  selectedFile.value = null
  result.value = null
  message.value = ''
  isError.value = false
  if (fileInput.value) {
    fileInput.value.value = ''
  }
  revokePreviewUrl()
}

async function parse() {
  if (!selectedFile.value) {
    isError.value = true
    message.value = '请先选择需要识别的票据图片。'
    return
  }

  submitting.value = true
  message.value = ''
  isError.value = false
  try {
    const payload = new FormData()
    payload.append('documentType', form.documentType)
    payload.append('file', selectedFile.value)
    result.value = await ocrApi.parse(payload)
    message.value = result.value.message
  } catch (error) {
    isError.value = true
    message.value = error.message || 'OCR 识别失败，请检查图片或稍后重试。'
  } finally {
    submitting.value = false
  }
}

function goToRecordForm() {
  if (!canFillRecord.value) return
  router.push({
    path: '/records/new',
    query: {
      source: '百度 OCR + DeepSeek',
      activityType: fields.value.activityType,
      subType: fields.value.subType,
      amount: fields.value.amount || '',
      note: `${activityTypeLabel.value}票据智能识别导入`
    }
  })
}

function formatFileSize(size) {
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(2)} MB`
}

function revokePreviewUrl() {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
}
</script>

<style scoped>
.submit-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.upload-preview {
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 18px;
  align-items: center;
  padding: 16px;
  border: 1px dashed rgba(47, 143, 91, 0.28);
  border-radius: 20px;
  background: rgba(47, 143, 91, 0.06);
}

.upload-preview img {
  width: 180px;
  height: 128px;
  object-fit: cover;
  border-radius: 16px;
  background: #fff;
}

@media (max-width: 720px) {
  .upload-preview {
    grid-template-columns: 1fr;
  }

  .upload-preview img {
    width: 100%;
  }
}
</style>
