import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api/authApi'

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)

  async function login(email, password) {
    const res = await authApi.login({ email, password })
    const { data } = res.data
    token.value = data.accessToken
    user.value = data.user
    localStorage.setItem('user', JSON.stringify(data.user))
    return data
  }

  async function signup(email, password, nickname) {
    const res = await authApi.signup({ email, password, nickname })
    return res.data
  }

  async function fetchMe() {
    const res = await authApi.getMe()
    user.value = res.data.data
    localStorage.setItem('user', JSON.stringify(res.data.data))
    return res.data.data
  }

  async function refresh() {
    const res = await authApi.refresh()
    token.value = res.data.data.accessToken
  }

  async function logout() {
    try {
      if (token.value) await authApi.logout(token.value)
    } catch {}
    token.value = ''
    user.value = null
    localStorage.removeItem('user')
  }

  async function initialize() {
    const savedUser = localStorage.getItem('user')
    if (savedUser) user.value = JSON.parse(savedUser)
    try {
      await refresh()
      await fetchMe()
    } catch {
      user.value = null
      localStorage.removeItem('user')
    }
  }

  return { token, user, isLoggedIn, login, signup, fetchMe, refresh, logout, initialize }
})
