/**
 * API Client - Cliente HTTP centralizado
 *
 * Configuração do Axios com interceptors, timeout e tratamento de erros
 */

import axios, { AxiosError, AxiosInstance, AxiosRequestConfig } from 'axios';

import { env } from '../config/env';

export interface HttpClient {
  get<T>(url: string, config?: RequestConfig): Promise<T>;
  post<T>(url: string, data?: unknown, config?: RequestConfig): Promise<T>;
  put<T>(url: string, data?: unknown, config?: RequestConfig): Promise<T>;
  delete<T>(url: string, config?: RequestConfig): Promise<T>;
  patch<T>(url: string, data?: unknown, config?: RequestConfig): Promise<T>;
  setAuthToken(token: string): void;
  removeAuthToken(): void;
}

export interface RequestConfig {
  headers?: Record<string, string>;
  timeout?: number;
  params?: Record<string, string | number | boolean | undefined>;
}

export interface ApiError {
  message: string;
  status?: number;
  code?: string;
  data?: unknown;
}

export class ApiHttpClient implements HttpClient {
  private readonly axiosInstance: AxiosInstance;
  private authToken: string | null = null;
  private isRefreshing: boolean = false;
  private failedQueue: {
    resolve: (token: string) => void;
    reject: (error: unknown) => void;
  }[] = [];

  constructor(baseURL: string, timeout: number = 30000) {
    this.axiosInstance = axios.create({
      baseURL,
      timeout,
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private processQueue(error: unknown, token: string | null = null): void {
    // Processo todos os requests que estavam aguardando o refresh
    this.failedQueue.forEach(promise => {
      if (error) {
        promise.reject(error);
      } else if (token) {
        promise.resolve(token);
      }
    });

    // Limpo a fila após processar
    this.failedQueue = [];
  }

  private setupInterceptors(): void {
    // Request Interceptor
    this.axiosInstance.interceptors.request.use(
      config => {
        // Adiciono token automaticamente se existir
        if (this.authToken && config.headers) {
          config.headers.Authorization = `Bearer ${this.authToken}`;
        }

        // Log de debug em desenvolvimento
        if (env.enableDebugLogs) {
          console.log(`${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
        }

        return config;
      },
      error => {
        if (env.enableDebugLogs) {
          console.error('Request Error:', error);
        }
        return Promise.reject(error);
      }
    );

    // Response Interceptor
    this.axiosInstance.interceptors.response.use(
      response => {
        // Log de sucesso em desenvolvimento
        if (env.enableDebugLogs) {
          console.log(`${response.status} ${response.config.url}`);
        }
        return response;
      },
      async (error: AxiosError) => {
        const originalRequest = error.config as AxiosRequestConfig & { _retry?: boolean };

        // Log de erro em desenvolvimento
        if (env.enableDebugLogs) {
          console.error(`${error.response?.status} ${error.config?.url}`);
          console.log('[ApiClient] Response Data:', error.response?.data);
        }

        // Tratamento específico para 401 Unauthorized
        if (error.response?.status === 401 && !originalRequest._retry) {
          // Se é um erro de login/register, não tenta fazer refresh
          const url = error.config?.url || '';
          if (
            url.includes('/auth/login') ||
            url.includes('/auth/register') ||
            url.includes('/auth/google')
          ) {
            // Erro de autenticação no login - retorna o erro normalizado sem tentar refresh
            if (env.enableDebugLogs) {
              console.log('[ApiClient] Auth error on login endpoint, returning normalized error');
            }
            return Promise.reject(this.normalizeError(error));
          }

          // Se já estou tentando fazer refresh, coloco este request na fila
          if (this.isRefreshing) {
            console.log('[ApiClient] Request aguardando refresh em andamento');
            return new Promise((resolve, reject) => {
              this.failedQueue.push({
                resolve: (token: string) => {
                  // Atualizo o header com o novo token e refaço o request
                  if (originalRequest.headers) {
                    originalRequest.headers.Authorization = `Bearer ${token}`;
                  }
                  resolve(this.axiosInstance(originalRequest));
                },
                reject: (err: unknown) => {
                  reject(err);
                },
              });
            });
          }

          // Marco que já tentei fazer refresh deste request para evitar loop infinito
          originalRequest._retry = true;
          this.isRefreshing = true;

          try {
            console.log('[ApiClient] Token expirado, tentando refresh');

            // Importo dinamicamente para evitar dependência circular
            const { authService } = await import('../services/auth-service-instance');
            const newSession = await authService.refreshToken();

            if (newSession) {
              console.log('[ApiClient] Refresh bem-sucedido, refazendo requests');

              // Atualizo o token no cliente
              this.setAuthToken(newSession.accessToken);

              // Processo a fila de requests que estavam aguardando
              this.processQueue(null, newSession.accessToken);

              // Atualizo o header do request original e refaço
              if (originalRequest.headers) {
                originalRequest.headers.Authorization = `Bearer ${newSession.accessToken}`;
              }

              return this.axiosInstance(originalRequest);
            } else {
              // Refresh retornou null, significa que não havia refresh token válido
              console.warn('[ApiClient] Refresh falhou: nenhum refresh token disponível');
              this.processQueue(new Error('Sessão expirada'), null);
              this.removeAuthToken();

              // TODO: Disparar evento de logout global aqui se necessário
              return Promise.reject(error);
            }
          } catch (refreshError) {
            console.error('[ApiClient] Erro ao fazer refresh:', refreshError);

            // Processo a fila rejeitando todos os requests
            this.processQueue(refreshError, null);
            this.removeAuthToken();

            // TODO: Disparar evento de logout global aqui se necessário
            return Promise.reject(refreshError);
          } finally {
            this.isRefreshing = false;
          }
        }

        // Tratamento de outros erros HTTP
        if (error.response) {
          const status = error.response.status;

          switch (status) {
            case 403:
              console.warn('[ApiClient] Forbidden - Sem permissão para acessar este recurso');
              break;

            case 404:
              console.warn('[ApiClient] Not Found - Recurso não encontrado');
              break;

            case 429:
              console.warn('[ApiClient] Too Many Requests - Rate limit excedido');
              break;

            case 500:
              console.error('[ApiClient] Server Error - Erro interno do servidor');
              break;

            case 503:
              console.error(
                '[ApiClient] Service Unavailable - Serviço temporariamente indisponível'
              );
              break;
          }
        } else if (error.request) {
          // Requisição foi feita mas sem resposta (timeout, sem conexão)
          console.error('[ApiClient] Network Error - Sem resposta do servidor');
        } else {
          // Erro ao configurar a requisição
          console.error('[ApiClient] Request Setup Error:', error.message);
        }

        return Promise.reject(this.normalizeError(error));
      }
    );
  }

  private normalizeError(error: AxiosError): ApiError {
    const responseData = error.response?.data as
      | { message?: string }
      | { error?: { message?: string } };

    // Tenta extrair a mensagem do backend de várias posições possíveis
    let message: string | undefined;

    if (typeof responseData === 'object' && responseData !== null) {
      message = (responseData as any).message || (responseData as any).error?.message;
    }

    const finalMessage = message || error.message || 'Erro desconhecido';

    if (error.response?.status === 401 || error.response?.status === 400) {
      console.log('[ApiClient] Error response data:', JSON.stringify(error.response?.data));
      console.log('[ApiClient] Extracted message:', finalMessage);
    }

    return {
      message: finalMessage,
      status: error.response?.status,
      code: error.code,
      data: error.response?.data,
    };
  }

  setAuthToken(token: string): void {
    this.authToken = token;
  }

  removeAuthToken(): void {
    this.authToken = null;
  }

  async get<T>(url: string, config?: RequestConfig): Promise<T> {
    const response = await this.axiosInstance.get<T>(url, config as AxiosRequestConfig);
    return response.data;
  }

  async post<T>(url: string, data?: unknown, config?: RequestConfig): Promise<T> {
    const response = await this.axiosInstance.post<T>(url, data, config as AxiosRequestConfig);
    return response.data;
  }

  async put<T>(url: string, data?: unknown, config?: RequestConfig): Promise<T> {
    const response = await this.axiosInstance.put<T>(url, data, config as AxiosRequestConfig);
    return response.data;
  }

  async patch<T>(url: string, data?: unknown, config?: RequestConfig): Promise<T> {
    const response = await this.axiosInstance.patch<T>(url, data, config as AxiosRequestConfig);
    return response.data;
  }

  async delete<T>(url: string, config?: RequestConfig): Promise<T> {
    const response = await this.axiosInstance.delete<T>(url, config as AxiosRequestConfig);
    return response.data;
  }
}

// Lazy singleton instance - será criado na primeira vez que for acessado
let _apiClient: ApiHttpClient | null = null;

export function getApiClient(): ApiHttpClient {
  if (!_apiClient) {
    _apiClient = new ApiHttpClient(env.apiUrl, env.apiTimeout);
  }
  return _apiClient;
}

// Export para compatibilidade (usa lazy initialization)
export const apiClient = new Proxy({} as ApiHttpClient, {
  get(_target, prop) {
    return (getApiClient() as any)[prop];
  },
});
