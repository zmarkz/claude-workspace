import { create } from 'zustand'
import { User, AuthResponse } from '../types'

interface AuthState {
  token: string | null
  user: User | null
  login: (response: AuthResponse) => void
  logout: () => void
  isAuthenticated: () => boolean
  setToken: (token: string | null) => void
  setUser: (user: User | null) => void
}

export const useAuthStore = create<AuthState>((set, get) => {
  const initializeFromStorage = () => {
    const token = localStorage.getItem('authToken')
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null
    return { token, user }
  }

  const initial = initializeFromStorage()

  return {
    token: initial.token,
    user: initial.user,

    login: (response: AuthResponse) => {
      localStorage.setItem('authToken', response.token)
      localStorage.setItem('user', JSON.stringify(response.user))
      set({ token: response.token, user: response.user })
    },

    logout: () => {
      localStorage.removeItem('authToken')
      localStorage.removeItem('user')
      set({ token: null, user: null })
    },

    isAuthenticated: () => {
      const { token } = get()
      return !!token
    },

    setToken: (token: string | null) => {
      if (token) {
        localStorage.setItem('authToken', token)
      } else {
        localStorage.removeItem('authToken')
      }
      set({ token })
    },

    setUser: (user: User | null) => {
      if (user) {
        localStorage.setItem('user', JSON.stringify(user))
      } else {
        localStorage.removeItem('user')
      }
      set({ user })
    }
  }
})
