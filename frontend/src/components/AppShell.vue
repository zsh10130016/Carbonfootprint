<template>
  <div class="page-shell">
    <header class="shell-header">
      <div class="container header-inner">
        <RouterLink class="brand" to="/dashboard">
          <span class="brand-badge">CF</span>
          <div>
            <strong>个人碳足迹管理系统</strong>
            <div class="muted brand-note">记录、核算、分析和低碳行动</div>
          </div>
        </RouterLink>
        <nav class="nav-links">
          <RouterLink v-for="item in navItems" :key="item.to" :to="item.to" class="nav-link">{{ item.label }}</RouterLink>
        </nav>
        <div class="user-box">
          <div>
            <div class="user-name">{{ authStore.user?.fullName || authStore.user?.username }}</div>
            <div class="muted user-subtitle">{{ authStore.user?.email }}</div>
          </div>
          <button class="button-secondary" @click="logout">退出</button>
        </div>
      </div>
    </header>
    <main class="container page-content">
      <slot />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const router = useRouter()

const navItems = [
  { label: '仪表盘', to: '/dashboard' },
  { label: '新增记录', to: '/records/new' },
  { label: '记录列表', to: '/records' },
  { label: '统计分析', to: '/stats' },
  { label: '智能建议', to: '/advice' },
  { label: '社区排行', to: '/community' }
]

function logout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.shell-header {
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1px solid rgba(47, 143, 91, 0.1);
  background: rgba(245, 250, 241, 0.78);
  backdrop-filter: blur(10px);
}

.header-inner {
  display: grid;
  grid-template-columns: 1.2fr 1.3fr 1fr;
  gap: 20px;
  align-items: center;
  padding: 18px 0;
}

.brand {
  display: flex;
  gap: 14px;
  align-items: center;
}

.brand-badge {
  width: 52px;
  height: 52px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  color: #fff;
  background: linear-gradient(135deg, var(--brand), var(--warm));
}

.brand-note {
  margin-top: 4px;
  font-size: 0.9rem;
}

.nav-links {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.nav-link {
  padding: 10px 14px;
  border-radius: 999px;
  color: var(--muted);
}

.nav-link.router-link-active {
  color: var(--brand-deep);
  background: rgba(47, 143, 91, 0.1);
}

.user-box {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 14px;
}

.user-name {
  font-weight: 700;
}

.user-subtitle {
  font-size: 0.85rem;
}

.page-content {
  padding: 30px 0 56px;
}

@media (max-width: 1024px) {
  .header-inner {
    grid-template-columns: 1fr;
  }

  .nav-links,
  .user-box {
    justify-content: flex-start;
  }
}
</style>
