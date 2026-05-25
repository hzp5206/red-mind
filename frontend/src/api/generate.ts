import { http } from './http';
import { AiRuntimeInfo, ApiResponse, GenerateRequest, GenerateResponse, GeneratedVersion } from '../types';

export function generateCopy(payload: GenerateRequest) {
  return http.post<ApiResponse<GenerateResponse>>('/generate', payload);
}

export interface OptimizePayload {
  option:
    | 'concise'
    | 'rich'
    | 'emoji'
    | 'rewrite_opening'
    | 'stronger_hook'
    | 'more_emotional'
    | 'more_collectible'
    | 'more_natural'
    | 'stronger_cta';
  title: string;
  content: string;
  tags: string[];
}

export function optimizeCopy(payload: OptimizePayload) {
  return http.post<
    ApiResponse<{
      title: string;
      content: string;
      tags: string[];
    }>
  >('/generate/optimize', payload);
}

export function reviewGeneratedVersion(payload: {
  request: GenerateRequest;
  version: GeneratedVersion;
}) {
  return http.post<ApiResponse<GeneratedVersion>>('/generate/review', payload);
}

export function getAiRuntimeInfo() {
  return http.get<ApiResponse<AiRuntimeInfo>>('/generate/runtime-info');
}

export async function generateCopyStream(
  payload: GenerateRequest,
  onEvent: (event: string, data: string) => void,
) {
  const token = localStorage.getItem('redmind_token');
  const response = await fetch('/api/v1/generate/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok || !response.body) {
    throw new Error('流式生成失败');
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder('utf-8');
  let buffer = '';

  while (true) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }
    buffer += decoder.decode(value, { stream: true });
    const chunks = buffer.split('\n\n');
    buffer = chunks.pop() || '';

    chunks.forEach((chunk) => {
      const lines = chunk.split('\n');
      const eventLine = lines.find((line) => line.startsWith('event:'));
      const dataLine = lines.find((line) => line.startsWith('data:'));
      if (eventLine && dataLine) {
        onEvent(eventLine.replace('event:', '').trim(), dataLine.replace('data:', '').trim());
      }
    });
  }
}
