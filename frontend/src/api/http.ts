import axios from 'axios';

export const http = axios.create({
  baseURL: '/api/v1',
  timeout: 20000,
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('redmind_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
