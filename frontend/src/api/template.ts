import { http } from './http';
import { ApiResponse, TemplateItem } from '../types';

export function getTemplates(category?: string) {
  return http.get<ApiResponse<TemplateItem[]>>('/templates', { params: { category } });
}
