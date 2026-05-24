<template>
  <div class="login">
    <h1>登录</h1>
    <form @submit.prevent="handleLogin">
      <div class="form-group">
        <label for="username">用户名 / 手机号</label>
        <input type="text" id="username" v-model.trim="form.username" autocomplete="username" required>
      </div>
      <div class="form-group">
        <label for="password">密码</label>
        <input type="password" id="password" v-model="form.password" autocomplete="current-password" required>
      </div>
      <button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '登录中' : '登录' }}</button>
      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
      <p>还没有账号？<router-link to="/register">立即注册</router-link></p>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useSessionStore } from '../stores/session'

const router = useRouter()
const sessionStore = useSessionStore()
const form = ref({
  username: '',
  password: ''
})
const errorMessage = ref('')
const isSubmitting = ref(false)

const handleLogin = async () => {
  try {
    isSubmitting.value = true
    errorMessage.value = ''
    const response = await axios.post('/api/auth/login', {
      username: form.value.username.trim(),
      password: form.value.password
    })
    sessionStore.setSession(response.data)
    router.push('/')
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '登录失败，请检查用户名、手机号和密码'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.login {
  max-width: 400px;
  margin: 0 auto;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-top: 100px;
}

h1 {
  text-align: center;
  color: #333;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  color: #666;
}

input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

button {
  width: 100%;
  padding: 10px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  margin-top: 10px;
}

button:hover {
  background-color: #45a049;
}

button:disabled,
button:disabled:hover {
  background-color: #b9c7bd;
  cursor: not-allowed;
}

p {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
}

.error-message {
  padding: 9px 10px;
  border-radius: 4px;
  background-color: #ffebee;
  color: #c62828;
}

a {
  color: #4CAF50;
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}
</style>
