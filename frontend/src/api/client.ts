import axios, {AxiosError, InternalAxiosRequestConfig} from 'axios';
import {API_BASE_URL} from '../config/api';
import {ApiError} from '../types';
import {getToken} from '../utils/storage';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    const token = await getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error),
);

export function parseApiError(error: unknown): ApiError {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiError>;
    if (axiosError.response?.data?.message) {
      return {
        status: axiosError.response.status,
        message: axiosError.response.data.message,
        errors: axiosError.response.data.errors,
      };
    }
    if (axiosError.code === 'ECONNABORTED') {
      return {status: 0, message: 'Request timed out. Check your connection.'};
    }
    if (!axiosError.response) {
      return {
        status: 0,
        message: 'Cannot reach server. Ensure backend is running.',
      };
    }
    return {
      status: axiosError.response.status,
      message: axiosError.message,
    };
  }
  return {status: 0, message: 'An unexpected error occurred.'};
}
