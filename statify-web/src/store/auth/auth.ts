import { defineStore } from 'pinia';
import { coreApi } from '../../api/http';
import type { CoreErrorResponse } from '../../api/CoreErrorResponse';
import type { AxiosError } from 'axios';
import type { AuthUser, LoginResponse, RegisterResponse } from './types';

export const authStore = defineStore('auth', {
  state: () => ({
    currentUser: null as AuthUser | null,
    loading: false,
    error: null as string | null,
    accessToken: null as string | null,
  }),

  actions: {
    async init() {
      await this.refresh();
    },

    async register(payload: { email: string; password: string }): Promise<AuthUser> {
      this.loading = true;
      this.error = null;

      try {
        const { data } = await coreApi.post<RegisterResponse>('/api/auth/register', payload);
        this.accessToken = data.accessToken;
        this.currentUser = data.user;
        return data.user;
      } catch (err: any) {
        const e = err as AxiosError<CoreErrorResponse>;
        this.error = e.response?.data.message || 'Register failure';
        throw e;
      } finally {
        this.loading = false;
      }
    },

    async login(payload: { email: string; password: string }): Promise<AuthUser> {
      this.loading = true;
      this.error = null;

      try {
        const { data } = await coreApi.post<LoginResponse>('/api/auth/login', payload);
        this.accessToken = data.accessToken;
        this.currentUser = data.user;
        return data.user;
      } catch (err: any) {
        const e = err as AxiosError<CoreErrorResponse>;
        this.error = e.response?.data?.message || 'Login failure';
        throw e;
      } finally {
        this.loading = false;
      }
    },

    async fetchCurrentUser() {
      this.loading = true;
      this.error = null;

      try {
        const { data } = await coreApi.get<AuthUser>('/api/auth/me');
        this.currentUser = data;
        return data;
      } catch (err: any) {
        const e = err as AxiosError<CoreErrorResponse>;
        this.error = e.response?.data?.message || 'Fetch current user failure';
        this.currentUser = null;
        throw e;
      } finally {
        this.loading = false;
      }
    },

    async refresh(): Promise<string | null> {
      this.loading = true;
      this.error = null;

      try {
        const { data } = await coreApi.post<{ accessToken: string }>('/api/auth/refresh');
        this.accessToken = data.accessToken;

        await this.fetchCurrentUser();
      } catch (err: any) {
        const e = err as AxiosError<CoreErrorResponse>;
        const status = e.response?.status;

        if (status === 401) {
          this.currentUser = null;
          this.accessToken = null;

          return null;
        }

        this.error = e.response?.data?.message || 'Refresh failure';
        this.currentUser = null;
        throw e;
      } finally {
        this.loading = false;
      }

      return this.accessToken;
    },

    async logout() {
      this.loading = true;
      this.error = null;

      try {
        await coreApi.post<void>('/api/auth/logout');
      } catch (err: any) {
        const e = err as AxiosError<CoreErrorResponse>;
        this.error = e.response?.data?.message || 'Logout failure';
        throw e;
      } finally {
        this.loading = false;
        this.currentUser = null;
        this.accessToken = null;
      }

      this.accessToken = null;
      this.currentUser = null;
    },

    // Methods that only call API without updating state (for cases when page will reload immediately)
    async loginWithoutStateUpdate(payload: { email: string; password: string }): Promise<void> {
      await coreApi.post<LoginResponse>('/api/auth/login', payload);
    },

    async registerWithoutStateUpdate(payload: { email: string; password: string }): Promise<void> {
      await coreApi.post<RegisterResponse>('/api/auth/register', payload);
    },

    async logoutWithoutStateUpdate(): Promise<void> {
      await coreApi.post<void>('/api/auth/logout');
    },
  },
});
