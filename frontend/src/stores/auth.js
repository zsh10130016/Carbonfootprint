import { defineStore } from 'pinia'

import { authApi, userApi } from '../api/modules'

const TOKEN_KEY = 'carbonfootprint-token'
const USER_KEY = 'carbonfootprint-user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: JSON.parse(localStorage.getItem(USER_KEY) || 'null')
  }),
  actions: {
    persistSession(token, user) {
      // Keep login state in localStorage so refreshes still preserve the session.
      this.token = token
      this.user = user
      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    },
    async login(payload) {
      const response = await authApi.login(payload)
      this.persistSession(response.token, response.user)
      return response
    },
    async register(payload) {
      const response = await authApi.register(payload)
      this.persistSession(response.token, response.user)
      return response
    },
    async fetchProfile() {
      if (!this.token) return null
      try {
        const profile = await userApi.getMe()
        this.setUser(profile)
        return profile
      } catch (error) {
        this.logout()
        throw error
      }
    },
    setUser(user) {
      this.user = user
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})
