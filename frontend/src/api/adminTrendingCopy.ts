import { http } from './http';
import { ApiResponse, PaginatedResponse, TrendingAnalysis, TrendingDashboard, TrendingItem, TrendingTask } from '../types';

export interface TrendingTaskPayload {
  id?: number;
  taskName: string;
  platformCode: string;
  keywords: string;
  fetchLimit: number;
  cronExpr: string;
  providerCode: string;
  enabled: boolean;
}

export function getTrendingDashboard() {
  return http.get<ApiResponse<TrendingDashboard>>('/admin/trending-copies/dashboard');
}

export function getTrendingTasks() {
  return http.get<ApiResponse<TrendingTask[]>>('/admin/trending-copies/tasks');
}

export function saveTrendingTask(payload: TrendingTaskPayload) {
  return http.post<ApiResponse<TrendingTask>>('/admin/trending-copies/tasks', payload);
}

export function deleteTrendingTask(id: number) {
  return http.delete<ApiResponse<null>>(`/admin/trending-copies/tasks/${id}`);
}

export function triggerTrendingTask(id: number) {
  return http.post<ApiResponse<number>>(`/admin/trending-copies/tasks/${id}/trigger`);
}

export function getTrendingItems(params: {
  keyword?: string;
  platformCode?: string;
  page: number;
  pageSize: number;
}) {
  return http.get<ApiResponse<PaginatedResponse<TrendingItem>>>('/admin/trending-copies/items', { params });
}

export function getTrendingAnalysis(id: number) {
  return http.get<ApiResponse<TrendingAnalysis>>(`/admin/trending-copies/items/${id}/analysis`);
}

export function collectTrendingItem(id: number) {
  return http.post<ApiResponse<null>>(`/admin/trending-copies/items/${id}/collect`);
}
