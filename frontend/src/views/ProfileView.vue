<template>
  <AppShell>
    <div class="stack">
      <PanelCard title="个人中心" subtitle="维护个人资料、低碳目标和账户密码。">
        <div class="field-grid">
          <div>
            <div class="muted">当前账号</div>
            <h2 class="profile-value">{{ authStore.user?.username }}</h2>
          </div>
          <div>
            <div class="muted">当前邮箱</div>
            <h2 class="profile-value">{{ authStore.user?.email }}</h2>
          </div>
          <div>
            <div class="muted">姓名</div>
            <h2 class="profile-value">{{ authStore.user?.fullName }}</h2>
          </div>
          <div>
            <div class="muted">低碳目标</div>
            <h2 class="profile-value">{{ profileForm.carbonGoal }} kg</h2>
          </div>
        </div>
      </PanelCard>

      <div class="profile-grid">
        <PanelCard title="编辑资料" subtitle="保存后会同步更新右上角登录信息。">
          <form class="stack" @submit.prevent="saveProfile">
            <div class="field-grid">
              <div class="field">
                <label>姓名</label>
                <input v-model="profileForm.fullName" />
              </div>
              <div class="field">
                <label>邮箱</label>
                <input v-model="profileForm.email" />
              </div>
              <div class="field">
                <label>所在地区</label>
                <input v-model="profileForm.location" placeholder="比如：浙江杭州" />
              </div>
              <div class="field">
                <label>每周低碳目标(kg)</label>
                <input v-model="profileForm.carbonGoal" type="number" min="0" step="0.1" />
              </div>
            </div>
            <div class="field">
              <label>个人简介</label>
              <textarea v-model="profileForm.bio" rows="4" placeholder="介绍一下你的绿色生活习惯或项目角色" />
            </div>
            <p class="helper-text">每周低碳目标会作为你后续记录和分析时的参考值。</p>
            <p v-if="profileMessage" :class="['feedback', profileError ? 'error' : 'success']">{{ profileMessage }}</p>
            <button class="button-primary" type="submit" :disabled="profileSubmitting">
              {{ profileSubmitting ? '保存中...' : '保存资料' }}
            </button>
          </form>
        </PanelCard>

        <PanelCard title="修改密码" subtitle="更新登录密码和账户安全信息。">
          <form class="stack" @submit.prevent="changePassword">
            <div class="field">
              <label>旧密码</label>
              <input v-model="passwordForm.oldPassword" type="password" />
            </div>
            <div class="field">
              <label>新密码</label>
              <input v-model="passwordForm.newPassword" type="password" />
            </div>
            <div class="field">
              <label>确认新密码</label>
              <input v-model="passwordForm.confirmPassword" type="password" />
            </div>
            <p class="helper-text">密码长度需为 6 到 20 位，修改成功后下次登录使用新密码。</p>
            <p v-if="passwordMessage" :class="['feedback', passwordError ? 'error' : 'success']">{{ passwordMessage }}</p>
            <button class="button-primary" type="submit" :disabled="passwordSubmitting">
              {{ passwordSubmitting ? '提交中...' : '修改密码' }}
            </button>
          </form>
        </PanelCard>
      </div>
    </div>
  </AppShell>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'

import { userApi } from '../api/modules'
import AppShell from '../components/AppShell.vue'
import PanelCard from '../components/PanelCard.vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const profileSubmitting = ref(false)
const passwordSubmitting = ref(false)
const profileMessage = ref('')
const passwordMessage = ref('')
const profileError = ref(false)
const passwordError = ref(false)

const profileForm = reactive({
  fullName: '',
  email: '',
  location: '',
  bio: '',
  carbonGoal: 50
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

watch(
  () => authStore.user,
  (user) => {
    profileForm.fullName = user?.fullName || ''
    profileForm.email = user?.email || ''
    profileForm.location = user?.location || ''
    profileForm.bio = user?.bio || ''
    profileForm.carbonGoal = user?.carbonGoal ?? 50
  },
  { immediate: true }
)

async function saveProfile() {
  const validationMessage = validateProfileForm()
  if (validationMessage) {
    profileError.value = true
    profileMessage.value = validationMessage
    return
  }

  profileSubmitting.value = true
  profileMessage.value = ''
  profileError.value = false
  try {
    const updated = await userApi.updateMe({
      ...profileForm,
      carbonGoal: Number(profileForm.carbonGoal)
    })
    authStore.setUser(updated)
    profileMessage.value = '个人资料已更新。'
  } catch (error) {
    profileError.value = true
    profileMessage.value = error.message
  } finally {
    profileSubmitting.value = false
  }
}

async function changePassword() {
  const validationMessage = validatePasswordForm()
  if (validationMessage) {
    passwordError.value = true
    passwordMessage.value = validationMessage
    return
  }

  passwordSubmitting.value = true
  passwordMessage.value = ''
  passwordError.value = false
  try {
    await userApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    passwordMessage.value = '密码修改成功。'
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    passwordError.value = true
    passwordMessage.value = error.message
  } finally {
    passwordSubmitting.value = false
  }
}

function validateProfileForm() {
  if (!profileForm.fullName.trim()) return '请输入姓名。'
  if (!profileForm.email.trim()) return '请输入邮箱。'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(profileForm.email.trim())) return '请输入正确的邮箱格式。'
  if (Number(profileForm.carbonGoal) < 0) return '每周低碳目标不能小于 0。'
  return ''
}

function validatePasswordForm() {
  if (!passwordForm.oldPassword) return '请输入旧密码。'
  if (!passwordForm.newPassword) return '请输入新密码。'
  if (passwordForm.newPassword.length < 6 || passwordForm.newPassword.length > 20) {
    return '新密码长度需在 6 到 20 位之间。'
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    return '两次输入的新密码不一致。'
  }
  return ''
}
</script>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.profile-value {
  margin: 8px 0 0;
}

@media (max-width: 960px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
