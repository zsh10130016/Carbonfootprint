<template>
  <AppShell>
    <PanelCard :title="articleTitle" :subtitle="articleSubtitle">
      <div v-if="errorMessage" class="feedback error">{{ errorMessage }}</div>
      <div v-else-if="loading" class="empty-state">正在加载资讯详情...</div>
      <div class="stack" v-else-if="article.id">
        <img class="detail-cover" :src="article.coverImage" :alt="article.title" />
        <p class="article-content">{{ article.content }}</p>
        <RouterLink class="button-secondary back-link" to="/community">返回社区页</RouterLink>
      </div>
      <div v-else class="empty-state">未找到对应的资讯内容。</div>
    </PanelCard>
  </AppShell>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

import { articleApi } from '../api/modules'
import AppShell from '../components/AppShell.vue'
import PanelCard from '../components/PanelCard.vue'

const route = useRoute()
const loading = ref(true)
const errorMessage = ref('')
const article = ref({})
const articleTitle = computed(() => article.value.title || '资讯详情')
const articleSubtitle = computed(() => (
  article.value.id ? `${article.value.author} · ${formatDate(article.value.publishedAt)}` : '查看科普资讯内容'
))

onMounted(loadArticle)

async function loadArticle() {
  loading.value = true
  errorMessage.value = ''
  try {
    article.value = await articleApi.detail(route.params.id)
  } catch (error) {
    errorMessage.value = error.message || '资讯详情加载失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

function formatDate(value) {
  return value?.replace('T', ' ').slice(0, 16)
}
</script>

<style scoped>
.detail-cover {
  width: 100%;
  max-height: 420px;
  object-fit: cover;
  border-radius: 20px;
}

.article-content {
  margin: 0;
  line-height: 1.9;
  white-space: pre-line;
}

.back-link {
  width: fit-content;
}
</style>
