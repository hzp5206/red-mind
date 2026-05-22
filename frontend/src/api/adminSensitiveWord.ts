import { http } from './http';
import { ApiResponse, SensitiveWordItem } from '../types';

export function getSensitiveWords() {
  return http.get<ApiResponse<SensitiveWordItem[]>>('/admin/sensitive-words');
}

export function saveSensitiveWord(payload: SensitiveWordItem) {
  return http.post<ApiResponse<null>>('/admin/sensitive-words', payload);
}

export function deleteSensitiveWord(id: number) {
  return http.delete<ApiResponse<null>>(`/admin/sensitive-words/${id}`);
}

export function importSensitiveWords(content: string) {
  return http.post<ApiResponse<number>>('/admin/sensitive-words/import', { content });
}

export function exportSensitiveWords() {
  return http.get<ApiResponse<string>>('/admin/sensitive-words/export');
}
