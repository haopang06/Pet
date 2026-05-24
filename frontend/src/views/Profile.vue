<template>
  <div class="profile-page">
    <section class="profile-head">
      <div>
        <p class="section-label">个人中心</p>
        <h1>个人详情</h1>
      </div>
      <router-link class="back-link" to="/">返回控制台</router-link>
    </section>

    <div v-if="profileLoading" class="panel loading-panel">资料加载中...</div>

    <div v-else class="profile-grid">
      <section class="panel profile-card">
        <div class="panel-title">
          <div>
            <p class="section-label">基础资料</p>
            <h2>头像与账号</h2>
          </div>
        </div>

        <div class="avatar-block">
          <UserAvatar :avatar="avatarPreview" :name="displayName" size="xl" />
          <div class="avatar-actions">
            <strong>{{ displayName }}</strong>
            <span>{{ profile.phone || '未绑定手机号' }}</span>
            <div class="button-row">
              <input
                id="avatarInput"
                class="file-input"
                type="file"
                accept="image/*"
                @change="handleAvatarChange"
              >
              <label class="secondary-btn" for="avatarInput">选择图片</label>
              <button type="button" :disabled="avatarSaving || !avatarDraft" @click="saveAvatar">
                {{ avatarSaving ? '保存中' : '保存头像' }}
              </button>
            </div>
            <p class="hint">支持 JPG、PNG 图片，上传前会自动压缩。</p>
            <p class="status-message">{{ avatarStatus }}</p>
          </div>
        </div>

        <div class="info-grid">
          <div class="info-item">
            <span>用户名</span>
            <strong>{{ profile.username || '-' }}</strong>
          </div>
          <div class="info-item">
            <span>绑定手机号</span>
            <strong>{{ profile.phone || '未绑定' }}</strong>
          </div>
        </div>
      </section>

      <section class="panel password-card">
        <div class="panel-title">
          <div>
            <p class="section-label">安全设置</p>
            <h2>修改密码</h2>
          </div>
        </div>

        <form @submit.prevent="savePassword">
          <div class="form-group">
            <label for="oldPassword">原密码</label>
            <input
              id="oldPassword"
              v-model="passwordForm.oldPassword"
              type="password"
              autocomplete="current-password"
              required
            >
          </div>
          <div class="form-group">
            <label for="newPassword">新密码</label>
            <input
              id="newPassword"
              v-model="passwordForm.newPassword"
              type="password"
              autocomplete="new-password"
              required
            >
          </div>
          <div class="form-group">
            <label for="confirmPassword">确认新密码</label>
            <input
              id="confirmPassword"
              v-model="passwordForm.confirmPassword"
              type="password"
              autocomplete="new-password"
              required
            >
          </div>
          <button type="submit" :disabled="passwordSaving">
            {{ passwordSaving ? '提交中' : '提交修改' }}
          </button>
          <p class="status-message">{{ passwordStatus }}</p>
        </form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import UserAvatar from '../components/UserAvatar.vue'
import { useSessionStore } from '../stores/session'
import { fileToCompressedDataUrl } from '../utils/image'

const router = useRouter()
const sessionStore = useSessionStore()

const profile = ref({
  username: sessionStore.username,
  phone: sessionStore.phone,
  avatar: sessionStore.avatar
})
const profileLoading = ref(false)
const avatarDraft = ref('')
const avatarSaving = ref(false)
const avatarStatus = ref('')
const passwordSaving = ref(false)
const passwordStatus = ref('')
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const displayName = computed(() => profile.value.username || sessionStore.username || '用户')
const avatarPreview = computed(() => avatarDraft.value || profile.value.avatar || sessionStore.avatar)

const fetchProfile = async () => {
  if (!sessionStore.isLoggedIn) {
    router.push('/login')
    return
  }

  try {
    profileLoading.value = true
    const response = await axios.get(`/api/users/${sessionStore.userId}`)
    profile.value = {
      username: response.data.username || '',
      phone: response.data.phone || '',
      avatar: response.data.avatar || ''
    }
    sessionStore.updateProfile(response.data)
    avatarDraft.value = ''
  } catch (error) {
    avatarStatus.value = error.response?.data?.message || '资料加载失败，请稍后重试'
  } finally {
    profileLoading.value = false
  }
}

const handleAvatarChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) {
    return
  }

  try {
    avatarStatus.value = '图片处理中...'
    avatarDraft.value = await fileToCompressedDataUrl(file, 512, 0.82)
    avatarStatus.value = '图片已选择，点击保存头像后生效'
  } catch (error) {
    avatarStatus.value = error.message || '图片读取失败'
  } finally {
    event.target.value = ''
  }
}

const saveAvatar = async () => {
  if (!avatarDraft.value) {
    avatarStatus.value = '请先选择头像图片'
    return
  }

  try {
    avatarSaving.value = true
    const response = await axios.post(`/api/users/${sessionStore.userId}/avatar`, {
      avatar: avatarDraft.value
    })
    profile.value = {
      username: response.data.username || profile.value.username,
      phone: response.data.phone || profile.value.phone,
      avatar: response.data.avatar || ''
    }
    sessionStore.updateProfile(response.data)
    avatarDraft.value = ''
    avatarStatus.value = '头像已保存'
  } catch (error) {
    avatarStatus.value = error.response?.data?.message || '头像保存失败，请稍后重试'
  } finally {
    avatarSaving.value = false
  }
}

const savePassword = async () => {
  const { oldPassword, newPassword, confirmPassword } = passwordForm.value

  if (!oldPassword || !newPassword || !confirmPassword) {
    passwordStatus.value = '请完整填写原密码、新密码和确认密码'
    return
  }

  if (newPassword !== confirmPassword) {
    passwordStatus.value = '两次输入的新密码不一致'
    return
  }

  try {
    passwordSaving.value = true
    passwordStatus.value = ''
    const response = await axios.post(`/api/users/${sessionStore.userId}/password`, {
      oldPassword,
      newPassword,
      confirmPassword
    })
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
    passwordStatus.value = response.data?.message || '密码修改成功'
  } catch (error) {
    passwordStatus.value = error.response?.data?.message || '密码修改失败，请检查原密码后重试'
  } finally {
    passwordSaving.value = false
  }
}

onMounted(() => {
  sessionStore.hydrate()
  fetchProfile()
})
</script>

<style scoped>
.profile-page {
  max-width: 1120px;
  margin: 0 auto;
  padding: 18px 24px 34px;
}

.profile-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.section-label {
  margin-bottom: 6px;
  color: #60756a;
  font-size: 13px;
  font-weight: 700;
}

h1 {
  color: #1f2d26;
  font-size: 28px;
  line-height: 1.25;
}

.back-link {
  padding: 10px 14px;
  border-radius: 8px;
  background-color: #2e7d52;
  color: white;
  font-size: 14px;
  font-weight: 700;
  text-decoration: none;
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 400px);
  gap: 18px;
  align-items: start;
}

.panel {
  padding: 20px;
  border: 1px solid #e0e7e3;
  border-radius: 12px;
  background-color: white;
}

.loading-panel {
  color: #60756a;
}

.panel-title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.panel-title h2 {
  color: #1f2d26;
  font-size: 20px;
}

.avatar-block {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 18px;
  border-radius: 12px;
  background-color: #f7faf8;
}

.avatar-actions {
  min-width: 0;
}

.avatar-actions strong,
.avatar-actions span {
  display: block;
}

.avatar-actions strong {
  color: #1f2d26;
  font-size: 20px;
  margin-bottom: 4px;
}

.avatar-actions span {
  color: #66766c;
  font-size: 14px;
}

.button-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin: 14px 0 8px;
}

.file-input {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0 0 0 0);
  white-space: nowrap;
}

button,
.secondary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 38px;
  padding: 9px 14px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

button {
  background-color: #2e7d52;
  color: white;
}

.secondary-btn {
  background-color: #edf7ef;
  color: #2e7d52;
}

button:disabled {
  background-color: #a6b5ac;
  cursor: not-allowed;
}

.hint,
.status-message {
  color: #60756a;
  font-size: 13px;
}

.status-message {
  min-height: 20px;
  margin-top: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.info-item {
  padding: 14px;
  border: 1px solid #e5ebe8;
  border-radius: 10px;
  background-color: #fbfcfb;
}

.info-item span {
  display: block;
  color: #718178;
  font-size: 13px;
  margin-bottom: 8px;
}

.info-item strong {
  display: block;
  color: #21332a;
  font-size: 18px;
}

form {
  display: grid;
  gap: 14px;
}

.form-group {
  display: grid;
  gap: 7px;
}

label {
  color: #55665c;
  font-size: 14px;
  font-weight: 700;
}

input {
  width: 100%;
  padding: 11px 12px;
  border: 1px solid #d6ded9;
  border-radius: 8px;
  color: #25332b;
  font-size: 15px;
}

input:focus {
  outline: none;
  border-color: #2e7d52;
  box-shadow: 0 0 0 3px rgba(46, 125, 82, 0.12);
}

@media (max-width: 780px) {
  .profile-page {
    padding: 14px;
  }

  .profile-head,
  .avatar-block {
    align-items: stretch;
    flex-direction: column;
  }

  .profile-grid,
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
