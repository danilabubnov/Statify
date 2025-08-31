import axios, { AxiosError, AxiosHeaders, type InternalAxiosRequestConfig } from 'axios';
import type { CoreErrorResponse } from './CoreErrorResponse';

const coreApi = axios.create({
  baseURL: import.meta.env.VITE_STATIFY_CORE_URL,
  timeout: 5000,
  withCredentials: true,
  headers: {
    'Accept-Language': 'en,en-US;q=0.9',
  },
});

coreApi.interceptors.request.use(async (config: InternalAxiosRequestConfig) => {
  const { authStore } = await import('../store/auth/auth');
  const token = authStore().accessToken;

  if (token) {
    const headers = AxiosHeaders.from(config.headers);
    headers.set('Authorization', `Bearer ${token}`);
    config.headers = headers;
  }

  if (import.meta.env.DEV) {
    await new Promise((resolve) => setTimeout(resolve, 1500));
  }

  return config;
});

let refreshPromise: Promise<string | null> | null = null;

coreApi.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<CoreErrorResponse>) => {
    const originalRequest = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry && originalRequest.url !== '/api/auth/login') {
      if (originalRequest.url === '/api/auth/refresh') {
        const { authStore } = await import('../store/auth/auth');
        await authStore().logout();
        return Promise.reject(error);
      }

      originalRequest._retry = true;

      if (!refreshPromise) {
        const { authStore } = await import('../store/auth/auth');
        const store = authStore();
        refreshPromise = store.refresh().finally(() => {
          refreshPromise = null;
        });
      }

      const newToken = await refreshPromise;

      if (!newToken) {
        const { authStore } = await import('../store/auth/auth');
        await authStore().logout();
        return Promise.reject(error);
      }

      const headers = AxiosHeaders.from(originalRequest.headers);
      headers.set('Authorization', `Bearer ${newToken}`);
      originalRequest.headers = headers;

      return coreApi(originalRequest);
    }

    return Promise.reject(error);
  },
);

export { coreApi };
