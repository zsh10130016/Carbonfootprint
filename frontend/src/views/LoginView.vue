<template>
  <div class="login-page">
    <div class="login-hero">
      <p class="hero-tag">Carbon Footprint</p>
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

      <form class="stack" @submit.prevent="submit">
        <div class="field-grid">
          <div v-if="mode !== 'login'" class="field">
            <label>用户名</label>
            <input v-model="form.username" autocomplete="username" placeholder="请输入用户名" />
          </div>
          <div v-if="mode === 'login'" class="field">
            <label>{{ mode === 'login' ? '账号' : '邮箱' }}</label>
            <input v-model="form.account" autocomplete="username" placeholder="用户名或邮箱" />
          </div>
          <div v-else class="field">
            <label>邮箱</label>
            <input v-model="form.email" autocomplete="email" placeholder="请输入邮箱" />
          </div>
          <div v-if="mode === 'register'" class="field">
            <label>姓名</label>
            <input v-model="form.fullName" placeholder="请输入姓名" />
          </div>
          <div class="field">
            <label>{{ mode === 'reset' ? '新密码' : '密码' }}</label>
            <input v-model="form.password" :autocomplete="mode === 'login' ? 'current-password' : 'new-password'" type="password" placeholder="请输入密码" />
          </div>
        </div>
        <p class="helper-text">{{ modeHint }}</p>

        <p v-if="message" :class="['feedback', isError ? 'error' : 'success']">{{ message }}</p>

        <button class="button-primary auth-submit" type="submit" :disabled="submitting">
          {{ submitting ? '提交中...' : currentLabel }}
        </button>
      </form>
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
const modeHint = computed(() => (
  mode.value === 'login'
    ? '支持使用用户名或邮箱登录。'
    : mode.value === 'register'
      ? '注册后会自动进入系统，并创建个人碳足迹档案。'
      : '输入用户名、邮箱和新密码后即可完成密码重置。'
))

function switchMode(nextMode) {
  mode.value = nextMode
  message.value = ''
  isError.value = false
  form.password = ''
}

async function submit() {
  const validationMessage = validateForm()
  if (validationMessage) {
    isError.value = true
    message.value = validationMessage
    return
  }

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
    mode.value = 'login'
    form.account = form.username || form.email
    form.password = ''
  } catch (error) {
    isError.value = true
    message.value = error.message
  } finally {
    submitting.value = false
  }
}

function validateForm() {
  if (mode.value === 'login') {
    if (!form.account.trim()) return '请输入账号。'
    if (!form.password) return '请输入密码。'
    return ''
  }

  if (!form.username.trim()) return '请输入用户名。'
  if (!form.email.trim()) return '请输入邮箱。'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) return '请输入正确的邮箱格式。'
  if (!form.password) return mode.value === 'reset' ? '请输入新密码。' : '请输入密码。'
  if (form.password.length < 6 || form.password.length > 20) return '密码长度需在 6 到 20 位之间。'
  if (mode.value === 'register' && !form.fullName.trim()) return '请输入姓名。'
  return ''
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
