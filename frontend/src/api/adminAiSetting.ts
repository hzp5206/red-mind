import { http } from './http';
import { AiConnectivityTestResult, AiSetting, ApiResponse } from '../types';

export interface AiSettingPayload {
  provider: string;
  baseUrl: string;
  model: string;
  chatPath: string;
  apiKey?: string;
}

export function getAiSetting() {
  return http.get<ApiResponse<AiSetting>>('/admin/ai-settings');
}

export function saveAiSetting(payload: AiSettingPayload) {
  return http.post<ApiResponse<AiSetting>>('/admin/ai-settings', payload);
}

export function testAiSetting() {
  return http.post<ApiResponse<AiConnectivityTestResult>>('/admin/ai-settings/test');
}
