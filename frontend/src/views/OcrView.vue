<template>
  <AppShell>
    <div class="stack">
      <PanelCard title="OCR 智能识别" subtitle="当前使用 mock 识别器演示接口链路，后续可以直接替换为真实 OCR 服务。">
        <div class="stack">
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
              placeholder="可粘贴一段票据文本作为演示输入，例如：高铁票 杭州东到上海虹桥 15km"
            />
          </div>

          <div class="submit-row">
            <button class="button-primary" :disabled="submitting" @click="parse">
              {{ submitting ? '识别中...' : '开始识别' }}
            </button>
            <button class="button-secondary" type="button" @click="fillSample">填入示例文本</button>
          </div>
        </div>
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
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { ocrApi } from '../api/modules'
import AppShell from '../components/AppShell.vue'
import PanelCard from '../components/PanelCard.vue'

const router = useRouter()
const submitting = ref(false)
const result = ref(null)
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

async function parse() {
  submitting.value = true
  try {
    result.value = await ocrApi.parse({ ...form })
  } catch (error) {
    alert(error.message)
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
      source: 'OCR 识别',
      activityType: mappedActivityType.value,
      subType: result.value.fields?.subType || '',
      amount: result.value.fields?.amount || '',
      note: `${mappedActivityTypeLabel.value} OCR 识别结果`
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
