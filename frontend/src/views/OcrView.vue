<template>
  <AppShell>
    <div class="stack">
      <PanelCard title="票据解析" subtitle="将票据文本解析为结构化字段，并带入碳记录表单。当前为内置解析模式，后续可接入真实 OCR 服务。">
        <form class="stack" @submit.prevent="parse">
          <div class="field-grid">
            <div class="field">
              <label>票据类型</label>
              <select v-model="form.documentType">
                <option value="TRANSPORT_TICKET">交通票据</option>
                <option value="UTILITY_BILL">水电账单</option>
              </select>
            </div>
          </div>

          <div class="field">
            <label>原始文本</label>
            <textarea
              v-model="form.rawText"
              rows="7"
              placeholder="可粘贴交通票据或水电账单文本，例如：高铁票 杭州东到上海虹桥 15km"
            />
          </div>
          <p class="helper-text">解析完成后可以直接带入记录表单，再由系统继续完成碳核算和积分计算。</p>
          <p v-if="message" :class="['feedback', isError ? 'error' : 'info']">{{ message }}</p>

          <div class="submit-row">
            <button class="button-primary" type="submit" :disabled="submitting">
              {{ submitting ? '识别中...' : '开始识别' }}
            </button>
            <button class="button-secondary" type="button" @click="fillSample">快速填充参考文本</button>
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
              <input :value="result.fields?.subType || ''" readonly />
            </div>
            <div class="field">
              <label>推荐数值</label>
              <input :value="result.fields?.amount || ''" readonly />
            </div>
          </div>

          <div class="field">
            <label>识别字段</label>
            <pre class="result-box">{{ prettyResult }}</pre>
          </div>

          <div class="submit-row">
            <button class="button-primary" @click="goToRecordForm">带入记录表单</button>
          </div>
        </div>
      </PanelCard>
    </div>
  </AppShell>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import { ocrApi } from '../api/modules'
import AppShell from '../components/AppShell.vue'
import PanelCard from '../components/PanelCard.vue'

const router = useRouter()
const submitting = ref(false)
const result = ref(null)
const message = ref('')
const isError = ref(false)
const form = reactive({
  documentType: 'TRANSPORT_TICKET',
  rawText: ''
})

const mappedActivityType = computed(() => (
  form.documentType === 'UTILITY_BILL' ? 'HOME_ENERGY' : 'TRANSPORT'
))

const mappedActivityTypeLabel = computed(() => (
  mappedActivityType.value === 'HOME_ENERGY' ? '家庭用能' : '绿色出行'
))

const prettyResult = computed(() => JSON.stringify(result.value?.fields || {}, null, 2))

watch(
  () => form.documentType,
  () => {
    result.value = null
    message.value = ''
    isError.value = false
  }
)

async function parse() {
  if (!form.rawText.trim()) {
    isError.value = true
    message.value = '请先输入待解析的票据文本。'
    return
  }

  submitting.value = true
  message.value = ''
  isError.value = false
  try {
    result.value = await ocrApi.parse({ ...form })
    message.value = '解析完成，请确认识别字段后再带入记录表单。'
  } catch (error) {
    isError.value = true
    message.value = error.message || '票据解析失败，请检查输入内容后重试。'
  } finally {
    submitting.value = false
  }
}

function fillSample() {
  if (form.documentType === 'UTILITY_BILL') {
    form.rawText = '电费账单：本期用电 18 度，账单金额 12.6 元'
  } else {
    form.rawText = '交通票据：高铁行程 15 km，杭州东到上海虹桥'
  }
}

function goToRecordForm() {
  if (!result.value) return
  router.push({
    path: '/records/new',
    query: {
      source: '票据解析',
      activityType: mappedActivityType.value,
      subType: result.value.fields?.subType || '',
      amount: result.value.fields?.amount || '',
      note: `${mappedActivityTypeLabel.value}票据解析导入`
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

.result-box {
  margin: 0;
  padding: 14px;
  border-radius: 16px;
  background: rgba(47, 143, 91, 0.08);
  overflow: auto;
}
</style>
