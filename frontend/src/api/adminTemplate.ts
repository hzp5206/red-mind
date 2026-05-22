import { http } from './http';
import { ApiResponse, PaginatedResponse, TemplateItem, TemplateSavePayload } from '../types';

export function getAdminTemplates(params?: {
  category?: string;
  keyword?: string;
  isActive?: boolean;
  page?: number;
  pageSize?: number;
}) {
  return http.get<ApiResponse<PaginatedResponse<TemplateItem>>>('/templates/admin', { params });
}

export function createTemplate(payload: TemplateSavePayload) {
  return http.post<ApiResponse<TemplateItem>>('/templates/admin', payload);
}

export function updateTemplate(id: number, payload: TemplateSavePayload) {
  return http.put<ApiResponse<TemplateItem>>(`/templates/admin/${id}`, payload);
}

export function toggleTemplate(id: number, active: boolean) {
  return http.patch<ApiResponse<null>>(`/templates/admin/${id}/status?active=${active}`);
}

export function deleteTemplate(id: number) {
  return http.delete<ApiResponse<null>>(`/templates/admin/${id}`);
}
