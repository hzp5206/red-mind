import { ApiResponse, SelectOptionItem } from '../types';
import { http } from './http';

export function getTemplateCategories() {
  return http.get<ApiResponse<SelectOptionItem[]>>('/template-categories');
}

export function getAdminTemplateCategories() {
  return http.get<ApiResponse<SelectOptionItem[]>>('/template-categories/admin');
}

export function saveTemplateCategory(payload: {
  id?: number;
  categoryCode: string;
  categoryName: string;
  sortOrder?: number;
  isActive?: boolean;
}) {
  return http.post<ApiResponse<SelectOptionItem>>('/template-categories/admin', payload);
}

export function deleteTemplateCategory(id: number) {
  return http.delete<ApiResponse<null>>(`/template-categories/admin/${id}`);
}
