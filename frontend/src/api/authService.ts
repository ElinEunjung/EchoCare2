import apiClient from './apiClient';
import type { RegisterRequest, LoginRequest, AuthResponse } from '../types';

export const authService = {
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await apiClient.post('/api/auth/register', data);
    if (response.data.token) {
      localStorage.setItem('authToken', response.data.token);
    }
    return response.data;
  },

  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post('/api/auth/login', data);
    if (response.data.token) {
      localStorage.setItem('authToken', response.data.token);
    }
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('authToken');
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('authToken');
  },
};
