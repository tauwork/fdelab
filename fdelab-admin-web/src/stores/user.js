import { defineStore } from 'pinia'
import { login as loginApi, me } from '../api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('fdelab_token') || '',
    profile: null,
  }),
  actions: {
    async login(form) {
      const data = await loginApi(form)
      this.token = data.token
      this.profile = data.user
      localStorage.setItem('fdelab_token', data.token)
    },
    async fetchProfile() {
      this.profile = await me()
    },
    clear() {
      this.token = ''
      this.profile = null
      localStorage.removeItem('fdelab_token')
    },
  },
})
