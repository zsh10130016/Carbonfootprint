<template>
  <AppShell>
    <div class="stack">
      <section class="community-grid">
        <PanelCard title="我的社区名片" subtitle="汇总当前积分、排名和累计记录表现。">
          <div class="field-grid">
            <div>
              <div class="muted">当前排名</div>
              <h2>#{{ profile.rank }}</h2>
            </div>
            <div>
              <div class="muted">累计积分</div>
              <h2>{{ profile.totalPoints }}</h2>
            </div>
            <div>
              <div class="muted">累计记录</div>
              <h2>{{ profile.totalRecords }}</h2>
            </div>
            <div>
              <div class="muted">累计排放</div>
              <h2>{{ Number(profile.totalEmission || 0).toFixed(2) }} kg</h2>
            </div>
          </div>
        </PanelCard>

        <PanelCard title="全站排行" subtitle="基于积分流水聚合生成的低碳排行。">
          <div class="table-wrap" v-if="rankings.length">
            <table>
              <thead>
                <tr>
                  <th>排名</th>
                  <th>用户</th>
                  <th>积分</th>
                  <th>称号</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in rankings" :key="item.userId">
                  <td>#{{ item.rank }}</td>
                  <td>{{ item.fullName || item.username }}</td>
                  <td>{{ item.totalPoints }}</td>
                  <td>{{ item.badge }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div v-else class="empty-state">当前还没有排行数据，记录产生积分后会在这里展示。</div>
        </PanelCard>
      </section>

      <p v-if="errorMessage" class="feedback error">{{ errorMessage }}</p>
      <div v-else-if="loading" class="empty-state glass-card">正在加载社区数据...</div>

      <PanelCard v-else title="环保科普资讯" subtitle="浏览低碳生活知识、政策解读和日常减排建议。">
        <div v-if="articles.length" class="article-grid">
          <RouterLink
            v-for="article in articles"
            :key="article.id"
            :to="`/articles/${article.id}`"
            class="article-card"
          >
            <img :src="article.coverImage" :alt="article.title" />
            <div class="article-body">
              <h3>{{ article.title }}</h3>
              <p class="muted">{{ article.summary }}</p>
              <span class="article-meta">{{ article.author }}</span>
            </div>
          </RouterLink>
        </div>
        <div v-else class="empty-state">暂无资讯内容。</div>
      </PanelCard>
    </div>
  </AppShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'

import { articleApi, communityApi } from '../api/modules'
import AppShell from '../components/AppShell.vue'
import PanelCard from '../components/PanelCard.vue'

const loading = ref(true)
const errorMessage = ref('')
const profile = ref({ rank: '-', totalPoints: 0, totalRecords: 0, totalEmission: 0 })
const rankings = ref([])
const articles = ref([])

onMounted(loadCommunityData)

async function loadCommunityData() {
  loading.value = true
  errorMessage.value = ''
  try {
    ;[profile.value, rankings.value, articles.value] = await Promise.all([
      communityApi.me(),
      communityApi.rankings(),
      articleApi.list()
    ])
  } catch (error) {
    errorMessage.value = error.message || '社区数据加载失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.community-grid {
  display: grid;
  grid-template-columns: 1fr 1.1fr;
  gap: 18px;
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 18px;
}

.article-card {
  overflow: hidden;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid rgba(47, 143, 91, 0.12);
}

.article-card img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.article-body {
  padding: 18px;
}

.article-meta {
  color: var(--brand-deep);
}

@media (max-width: 960px) {
  .community-grid {
    grid-template-columns: 1fr;
  }
}
</style>
