import { http } from './http';
import { ApiResponse, HistoryRecord } from '../types';

export interface HistoryPageResponse {
  total: number;
  records: HistoryRecord[];
}

export function getHistory(params: {
  page: number;
  limit: number;
  style?: string;
  startDate?: string;
  endDate?: string;
}) {
  return http.get<ApiResponse<HistoryPageResponse>>('/history', { params });
}

export function deleteHistory(id: number) {
  return http.delete<ApiResponse<null>>(`/history/${id}`);
}

export function collectHistory(id: number) {
  return http.post<ApiResponse<null>>(`/history/${id}/collect`);
}
