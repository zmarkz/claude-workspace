import client from './client';
import type { AuthResponse, LoginRequest, RegisterRequest } from '../types';

export const login = (data: LoginRequest) =>
  client.post<AuthResponse>('/auth/login', data).then((r) => r.data);

export const register = (data: RegisterRequest) =>
  client.post<AuthResponse>('/auth/register', data).then((r) => r.data);
