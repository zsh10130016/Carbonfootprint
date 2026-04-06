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
          <div class="stack">
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
            <p v-if="profileMessage" :class="['feedback', profileError ? 'error' : 'success']">{{ profileMessage }}</p>
            <button class="button-primary" :disabled="profileSubmitting" @click="saveProfile">
              {{ profileSubmitting ? '保存中...' : '保存资料' }}
            </button>
          </div>
        </PanelCard>

        <PanelCard title="修改密码" subtitle="建议答辩演示时一起展示账号安全相关能力。">
          <div class="stack">
            <div class="field">
              <label>旧密码</label>
              <input v-model="passwordForm.oldPassword" type="password" />
            </div>
            <div class="field">
              <label>新密码</label>
              <input v-model="passwordForm.newPassword" type="password" />
            </div>
            <p v-if="passwordMessage" :class="['feedback', passwordError ? 'error' : 'success']">{{ passwordMessage }}</p>
            <button class="button-primary" :disabled="passwordSubmitting" @click="changePassword">
              {{ passwordSubmitting ? '提交中...' : '修改密码' }}
            </button>
          </div>
        </PanelCard>
      </div>
    </div>
  </AppShell>
</template>

<script setup>
import { reactive, ref } from 'vue'

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
  fullName: authStore.user?.fullName || '',
  email: authStore.user?.email || '',
  location: authStore.user?.location || '',
  bio: authStore.user?.bio || '',
  carbonGoal: authStore.user?.carbonGoal ?? 50
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: ''
})

async function saveProfile() {
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
  passwordSubmitting.value = true
  passwordMessage.value = ''
  passwordError.value = false
  try {
    await userApi.changePassword({ ...passwordForm })
    passwordMessage.value = '密码修改成功。'
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
  } catch (error) {
    passwordError.value = true
    passwordMessage.value = error.message
  } finally {
    passwordSubmitting.value = false
  }
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

@media (max-width: 960px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
