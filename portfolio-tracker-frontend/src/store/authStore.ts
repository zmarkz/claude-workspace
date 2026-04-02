import { create } from 'zustand';
import type { AuthResponse } from '../types';

interface AuthState {
  token: string | null;
  user: Omit<AuthResponse, 'token'> | null;
  isAuthenticated: boolean;
  setAuth: (auth: AuthResponse) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: localStorage.getItem('token'),
  user: (() => {
    const u = localStorage.getItem('user');
    return u ? JSON.parse(u) : null;
  })(),
  isAuthenticated: !!localStorage.getItem('token'),

  setAuth: (auth) => {
    localStorage.setItem('token', auth.token);
    const { token, ...user } = auth;
    localStorage.setItem('user', JSON.stringify(user));
    set({ token: auth.token, user, isAuthenticated: true });
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    set({ token: null, user: null, isAuthenticated: false });
  },
}));
