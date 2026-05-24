<template>
  <div class="register">
    <h1>注册</h1>
    <form @submit.prevent="handleRegister">
      <div class="form-group">
        <label for="username">用户名</label>
        <input type="text" id="username" v-model.trim="form.username" autocomplete="username" required>
      </div>
      <div class="form-group">
        <label for="phone">手机号</label>
        <input
          type="tel"
          id="phone"
          v-model.trim="form.phone"
          inputmode="numeric"
          maxlength="11"
          autocomplete="tel"
          required
        >
      </div>
      <div class="form-group">
        <label for="password">密码</label>
        <input type="password" id="password" v-model="form.password" autocomplete="new-password" required>
      </div>
      <div class="form-group">
        <label for="confirmPassword">确认密码</label>
        <input type="password" id="confirmPassword" v-model="form.confirmPassword" autocomplete="new-password" required>
      </div>
      <button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '注册中' : '注册' }}</button>
      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
      <p>已有账号？<router-link to="/login">立即登录</router-link></p>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const form = ref({
  username: '',
  password: '',
  phone: '',
  confirmPassword: ''
})
const errorMessage = ref('')
const isSubmitting = ref(false)
const phonePattern = /^1\d{10}$/

const handleRegister = async () => {
  const username = form.value.username.trim()
  const phone = form.value.phone.trim()
  const password = form.value.password
  const confirmPassword = form.value.confirmPassword

  if (!username || !phone || !password || !confirmPassword) {
    errorMessage.value = '用户名、手机号、密码和确认密码不能为空'
    return
  }

  if (!phonePattern.test(phone)) {
    errorMessage.value = '请输入有效的 11 位手机号'
    return
  }

  if (password !== confirmPassword) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }

  try {
    isSubmitting.value = true
    errorMessage.value = ''
    await axios.post('/api/auth/register', {
      username,
      phone,
      password,
      confirmPassword
    })
    router.push('/login')
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '注册失败，请检查用户名或手机号是否已存在'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.register {
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
