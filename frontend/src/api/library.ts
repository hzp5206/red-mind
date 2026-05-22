import { http } from './http';
import { ApiResponse, LibraryItem } from '../types';

export function getMyCollections() {
  return http.get<ApiResponse<LibraryItem[]>>('/library/collections');
}

export function deleteCollection(id: number) {
  return http.delete<ApiResponse<null>>(`/library/collections/${id}`);
}
