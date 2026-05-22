import { http } from './http';
import { ApiResponse } from '../types';

export interface PurifyResponse {
  cleanContent: string;
  replacedWords: { oldWord: string; newWord: string }[];
}

export function purifyContent(content: string) {
  return http.post<ApiResponse<PurifyResponse>>('/content/purify', { content });
}
