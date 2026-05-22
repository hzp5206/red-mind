import { http } from './http';
import { ApiResponse, UserProfile } from '../types';

export function getCurrentUser() {
  return http.get<ApiResponse<UserProfile>>('/users/me');
}
