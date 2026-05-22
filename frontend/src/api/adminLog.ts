import { ApiResponse, GenerationLogItem, OperationLogItem, PaginatedResponse } from '../types';
import { http } from './http';

export function getGenerationLogs(params?: {
  page?: number;
  pageSize?: number;
  userId?: number;
  style?: string;
  startDate?: string;
  endDate?: string;
}) {
  return http.get<ApiResponse<PaginatedResponse<GenerationLogItem>>>('/admin/logs/generations', { params });
}

export function getOperationLogs(params?: {
  page?: number;
  pageSize?: number;
  operatorId?: number;
  moduleName?: string;
  actionName?: string;
  startDate?: string;
  endDate?: string;
}) {
  return http.get<ApiResponse<PaginatedResponse<OperationLogItem>>>('/admin/logs/operations', { params });
}
