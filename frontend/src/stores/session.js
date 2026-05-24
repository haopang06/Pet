import { defineStore } from 'pinia'

const STORAGE_KEYS = {
  token: 'token',
  userId: 'userId',
  username: 'username',
  phone: 'phone',
  avatar: 'avatar'
}

const readStoredValue = (key) => {
  if (typeof window === 'undefined') {
    return ''
  }
  return localStorage.getItem(key) || ''
}

const writeStoredValue = (key, value) => {
  if (typeof window === 'undefined') {
    return
  }

  if (value === undefined || value === null || value === '') {
    localStorage.removeItem(key)
    return
  }

  localStorage.setItem(key, String(value))
}

export const useSessionStore = defineStore('session', {
  state: () => ({
    token: readStoredValue(STORAGE_KEYS.token),
    userId: readStoredValue(STORAGE_KEYS.userId),
    username: readStoredValue(STORAGE_KEYS.username),
    phone: readStoredValue(STORAGE_KEYS.phone),
    avatar: readStoredValue(STORAGE_KEYS.avatar)
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token && state.userId)
  },
  actions: {
    hydrate() {
      this.token = readStoredValue(STORAGE_KEYS.token)
      this.userId = readStoredValue(STORAGE_KEYS.userId)
      this.username = readStoredValue(STORAGE_KEYS.username)
      this.phone = readStoredValue(STORAGE_KEYS.phone)
      this.avatar = readStoredValue(STORAGE_KEYS.avatar)
    },
    setSession(payload = {}) {
      this.token = payload.token || ''
      this.userId = payload.userId != null ? String(payload.userId) : ''
      this.username = payload.username || ''
      this.phone = payload.phone || ''
      this.avatar = payload.avatar || ''

      writeStoredValue(STORAGE_KEYS.token, this.token)
      writeStoredValue(STORAGE_KEYS.userId, this.userId)
      writeStoredValue(STORAGE_KEYS.username, this.username)
      writeStoredValue(STORAGE_KEYS.phone, this.phone)
      writeStoredValue(STORAGE_KEYS.avatar, this.avatar)
    },
    updateProfile(payload = {}) {
      if (Object.prototype.hasOwnProperty.call(payload, 'username')) {
        this.username = payload.username || ''
        writeStoredValue(STORAGE_KEYS.username, this.username)
      }

      if (Object.prototype.hasOwnProperty.call(payload, 'phone')) {
        this.phone = payload.phone || ''
        writeStoredValue(STORAGE_KEYS.phone, this.phone)
      }

      if (Object.prototype.hasOwnProperty.call(payload, 'avatar')) {
        this.avatar = payload.avatar || ''
        writeStoredValue(STORAGE_KEYS.avatar, this.avatar)
      }
    },
    clearSession() {
      this.token = ''
      this.userId = ''
      this.username = ''
      this.phone = ''
      this.avatar = ''

      writeStoredValue(STORAGE_KEYS.token, '')
      writeStoredValue(STORAGE_KEYS.userId, '')
      writeStoredValue(STORAGE_KEYS.username, '')
      writeStoredValue(STORAGE_KEYS.phone, '')
      writeStoredValue(STORAGE_KEYS.avatar, '')
    }
  }
})
