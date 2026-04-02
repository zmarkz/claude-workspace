import client from './client'
import { AuthResponse, LoginRequest, RegisterRequest } from '../types'

export async function login(credentials: LoginRequest): Promise<AuthResponse> {
  const response = await client.post<AuthResponse>('/auth/login', credentials)
  return response.data
}

export async function register(data: RegisterRequest): Promise<AuthResponse> {
  const response = await client.post<AuthResponse>('/auth/register', data)
  return response.data
}

export async function logout(): Promise<void> {
  await client.post('/auth/logout')
}
