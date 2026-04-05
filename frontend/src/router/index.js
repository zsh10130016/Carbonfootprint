import { createRouter, createWebHistory } from 'vue-router'

import { useAuthStore } from '../stores/auth'

const LoginView = () => import('../views/LoginView.vue')
const DashboardView = () => import('../views/DashboardView.vue')
const RecordFormView = () => import('../views/RecordFormView.vue')
const RecordListView = () => import('../views/RecordListView.vue')
const StatsView = () => import('../views/StatsView.vue')
const AdviceView = () => import('../views/AdviceView.vue')
const CommunityView = () => import('../views/CommunityView.vue')
const ArticleDetailView = () => import('../views/ArticleDetailView.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginView },
    { path: '/', redirect: '/dashboard' },
    { path: '/dashboard', name: 'dashboard', component: DashboardView, meta: { requiresAuth: true } },
    { path: '/records/new', name: 'record-new', component: RecordFormView, meta: { requiresAuth: true } },
    { path: '/records', name: 'record-list', component: RecordListView, meta: { requiresAuth: true } },
    { path: '/stats', name: 'stats', component: StatsView, meta: { requiresAuth: true } },
    { path: '/advice', name: 'advice', component: AdviceView, meta: { requiresAuth: true } },
    { path: '/community', name: 'community', component: CommunityView, meta: { requiresAuth: true } },
    { path: '/articles/:id', name: 'article-detail', component: ArticleDetailView, meta: { requiresAuth: true } }
  ]
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  if (authStore.token && !authStore.user && to.path !== '/login') {
    await authStore.fetchProfile()
  }
  if (to.meta.requiresAuth && !authStore.token) {
    return { name: 'login' }
  }
  if (to.path === '/login' && authStore.token) {
    return { name: 'dashboard' }
  }
  return true
})

export default router
