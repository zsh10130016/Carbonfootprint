<template>
  <AppShell>
    <PanelCard :title="article.title" :subtitle="`${article.author} · ${formatDate(article.publishedAt)}`">
      <div class="stack" v-if="article.id">
        <img class="detail-cover" :src="article.coverImage" :alt="article.title" />
        <p class="article-content">{{ article.content }}</p>
        <RouterLink class="button-secondary back-link" to="/community">返回社区页</RouterLink>
      </div>
    </PanelCard>
  </AppShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

import { articleApi } from '../api/modules'
import AppShell from '../components/AppShell.vue'
import PanelCard from '../components/PanelCard.vue'

const route = useRoute()
const article = ref({})

onMounted(async () => {
  try {
    article.value = await articleApi.detail(route.params.id)
  } catch (error) {
    alert(error.message)
  }
})

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
