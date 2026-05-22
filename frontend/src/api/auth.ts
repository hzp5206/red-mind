import { http } from './http';
import { ApiResponse } from '../types';

export interface TokenResponse {
  userId: number;
  token: string;
  nickname: string;
  role?: string;
  roleCode?: string;
  permissions?: string[];
}

export function login(payload: { email: string; password: string }) {
  return http.post<ApiResponse<TokenResponse>>('/auth/login', payload);
}

export function register(payload: { email: string; nickname: string; password: string }) {
  return http.post<ApiResponse<TokenResponse>>('/auth/register', payload);
}
