import { ApiResponse, DashboardSummary } from '../types';
import { http } from './http';

export function getDashboardSummary() {
  return http.get<ApiResponse<DashboardSummary>>('/admin/dashboard');
}
