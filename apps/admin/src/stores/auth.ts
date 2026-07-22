import { defineStore } from 'pinia'
import type { AdminSession } from '@leisure-brew/api-contract'

const STORAGE_KEY = 'leisure-brew-admin-session'

function readSession(): AdminSession | null {
  try {
    const value = localStorage.getItem(STORAGE_KEY)
    return value ? (JSON.parse(value) as AdminSession) : null
  } catch {
    localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({ session: readSession() as AdminSession | null }),
  getters: {
    token: (state) => state.session?.token || '',
    employeeId: (state) => state.session?.id,
    displayName: (state) => state.session?.name || '门店伙伴',
  },
  actions: {
    save(session: AdminSession) {
      this.session = session
      localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
    },
    clear() {
      this.session = null
      localStorage.removeItem(STORAGE_KEY)
    },
  },
})
