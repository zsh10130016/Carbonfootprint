<template>
  <div class="login-page">
    <div class="login-hero">
      <p class="hero-tag">Graduation Project</p>
      <h1>个人碳足迹计算与管理系统</h1>
      <p class="hero-copy">
        把日常出行、家庭用能和饮食消费转成可量化的数据，再通过趋势、结构和建议帮你看清自己的低碳轨迹。
      </p>
      <div class="hero-grid">
        <div class="hero-chip">JWT 鉴权</div>
        <div class="hero-chip">碳核算</div>
        <div class="hero-chip">ECharts 分析</div>
        <div class="hero-chip">社区排行</div>
      </div>
    </div>

    <section class="glass-card auth-card">
      <div class="auth-tabs">
        <button
          v-for="item in modes"
          :key="item.value"
          :class="['tab-button', { active: mode === item.value }]"
          @click="switchMode(item.value)"
        >
          {{ item.label }}
        </button>
      </div>

      <div class="stack">
        <div class="field-grid">
          <div v-if="mode !== 'login'" class="field">
            <label>用户名</label>
            <input v-model="form.username" placeholder="请输入用户名" />
          </div>
          <div v-if="mode === 'login'" class="field">
            <label>{{ mode === 'login' ? '账号' : '邮箱' }}</label>
            <input v-model="form.account" placeholder="用户名或邮箱" />
          </div>
          <div v-else class="field">
            <label>邮箱</label>
            <input v-model="form.email" placeholder="请输入邮箱" />
          </div>
          <div v-if="mode === 'register'" class="field">
            <label>姓名</label>
            <input v-model="form.fullName" placeholder="请输入姓名" />
          </div>
          <div class="field">
            <label>{{ mode === 'reset' ? '新密码' : '密码' }}</label>
            <input v-model="form.password" type="password" placeholder="请输入密码" />
          </div>
        </div>

        <p v-if="message" :class="['feedback', isError ? 'error' : 'success']">{{ message }}</p>

        <button class="button-primary auth-submit" :disabled="submitting" @click="submit">
          {{ submitting ? '提交中...' : currentLabel }}
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { authApi } from '../api/modules'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const modes = [
  { value: 'login', label: '登录' },
  { value: 'register', label: '注册' },
  { value: 'reset', label: '重置密码' }
]

const mode = ref('login')
const submitting = ref(false)
const message = ref('')
const isError = ref(false)
const form = reactive({
  account: '',
  username: '',
  email: '',
  fullName: '',
  password: ''
})

const currentLabel = computed(() => (
  mode.value === 'login' ? '进入系统' : mode.value === 'register' ? '创建账号' : '重置密码'
))

function switchMode(nextMode) {
  mode.value = nextMode
  message.value = ''
  isError.value = false
}

async function submit() {
  submitting.value = true
  message.value = ''
  isError.value = false
  try {
    if (mode.value === 'login') {
      await authStore.login({ account: form.account, password: form.password })
      router.push('/dashboard')
      return
    }
    if (mode.value === 'register') {
      await authStore.register({
        username: form.username,
        email: form.email,
        fullName: form.fullName,
        password: form.password
      })
      router.push('/dashboard')
      return
    }
    await authApi.resetPassword({
      username: form.username,
      email: form.email,
      newPassword: form.password
    })
    message.value = '密码已重置，现在可以直接登录。'
  } catch (error) {
    isError.value = true
    message.value = error.message
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 28px;
  align-items: center;
  padding: 24px;
}

.login-hero {
  padding: 48px;
}

.hero-tag {
  display: inline-block;
  margin: 0 0 18px;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(47, 143, 91, 0.12);
  color: var(--brand-deep);
}

.login-hero h1 {
  font-size: clamp(2.5rem, 5vw, 4.4rem);
  line-height: 1.05;
  margin: 0 0 18px;
}

.hero-copy {
  max-width: 640px;
  color: var(--muted);
  line-height: 1.8;
  font-size: 1.02rem;
}

.hero-grid {
  margin-top: 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.hero-chip {
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(47, 143, 91, 0.12);
}

.auth-card {
  padding: 28px;
}

.auth-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
}

.tab-button {
  flex: 1;
  padding: 12px;
  border-radius: 999px;
  border: 1px solid rgba(47, 143, 91, 0.12);
  background: rgba(47, 143, 91, 0.06);
}

.tab-button.active {
  color: #fff;
  background: linear-gradient(135deg, var(--brand), var(--brand-deep));
}

.feedback {
  margin: 0;
  padding: 12px 14px;
  border-radius: 14px;
}

.feedback.error {
  background: rgba(199, 88, 79, 0.12);
  color: var(--danger);
}

.feedback.success {
  background: rgba(47, 143, 91, 0.12);
  color: var(--brand-deep);
}

.auth-submit {
  width: 100%;
}

@media (max-width: 960px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-hero,
  .auth-card {
    padding: 24px;
  }
}
</style>
