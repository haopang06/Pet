<template>
  <div class="app-shell">
    <header v-if="showTopBar" class="top-bar">
      <router-link class="brand" to="/">宠物智能喂养系统</router-link>
      <nav class="main-nav" aria-label="主导航">
        <router-link to="/">控制台</router-link>
        <router-link to="/pets">宠物信息</router-link>
        <router-link to="/feeding">智能喂养</router-link>
        <router-link to="/health">健康监测</router-link>
      </nav>
      <div class="top-actions">
        <div v-if="isLoggedIn" class="user-panel">
          <div class="avatar">{{ avatarText }}</div>
          <span class="username">{{ username }}</span>
          <button type="button" class="logout-btn" @click="logout">登出</button>
        </div>
        <button v-else type="button" class="login-btn" @click="goLogin">登录</button>
      </div>
    </header>

    <main :class="{ 'with-top-bar': showTopBar }">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const showTopBar = computed(() => !['/login', '/register'].includes(route.path))

const username = computed(() => {
  route.fullPath
  return localStorage.getItem('username') || ''
})

const isLoggedIn = computed(() => {
  route.fullPath
  return Boolean(localStorage.getItem('token') && localStorage.getItem('userId'))
})

const avatarText = computed(() => {
  return username.value ? username.value.slice(0, 1).toUpperCase() : '用'
})

const goLogin = () => {
  router.push('/login')
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
  router.push('/login')
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: Arial, sans-serif;
  background-color: #f3f6f4;
  color: #1f2d26;
}
</style>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.top-bar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 18px;
  min-height: 58px;
  padding: 0 24px;
  background-color: white;
  border-bottom: 1px solid #e0e7e3;
}

.brand {
  color: #1f2d26;
  font-size: 18px;
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;
}

.main-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-width: 0;
}

.main-nav a {
  padding: 8px 11px;
  border-radius: 6px;
  color: #55665c;
  font-size: 14px;
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;
}

.main-nav a:hover,
.main-nav a.router-link-active {
  background-color: #edf5f0;
  color: #2e7d52;
}

.top-actions {
  display: flex;
  justify-content: flex-end;
  min-width: 0;
}

.user-panel {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background-color: #2e7d52;
  color: white;
  font-weight: 700;
}

.username {
  color: #333;
  font-size: 14px;
}

.logout-btn,
.login-btn {
  padding: 7px 12px;
  border: none;
  border-radius: 6px;
  background-color: #2e7d52;
  color: white;
  cursor: pointer;
  font-size: 14px;
}

.logout-btn {
  background-color: #f44336;
}

.logout-btn:hover {
  background-color: #d32f2f;
}

.login-btn:hover {
  background-color: #256944;
}

.with-top-bar {
  padding-top: 12px;
}

@media (max-width: 900px) {
  .top-bar {
    grid-template-columns: 1fr auto;
    padding: 10px 14px;
  }

  .main-nav {
    grid-column: 1 / -1;
    grid-row: 2;
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: 2px;
  }
}

@media (max-width: 520px) {
  .brand {
    font-size: 16px;
  }

  .username {
    display: none;
  }

  .main-nav a {
    padding: 7px 9px;
    font-size: 13px;
  }
}
</style>
