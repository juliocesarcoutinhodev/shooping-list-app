/**
 * Environment Configuration
 * Centraliza acesso às variáveis de ambiente usando expo-constants
 */

import Constants from 'expo-constants';

interface EnvConfig {
  apiUrl: string;
  apiTimeout: number;
  appName: string;
  appEnv: 'development' | 'staging' | 'production';
  enableMockApi: boolean;
  enableDebugLogs: boolean;
  googleClientId: string;
}

function getEnvVar(key: string, defaultValue?: string): string {
  const extra = Constants.expoConfig?.extra;
  const value = extra?.[key] || process.env[key];

  if (!value && !defaultValue) {
    console.warn(`Environment variable ${key} is not set`);
  }

  return value || defaultValue || '';
}

function getBooleanEnvVar(key: string, defaultValue: boolean = false): boolean {
  const value = getEnvVar(key);
  if (!value) return defaultValue;
  return value.toLowerCase() === 'true';
}

function getNumberEnvVar(key: string, defaultValue: number): number {
  const value = getEnvVar(key);
  if (!value) return defaultValue;
  const parsed = parseInt(value, 10);
  return isNaN(parsed) ? defaultValue : parsed;
}

export const env: EnvConfig = {
  apiUrl: getEnvVar('API_URL', 'http://localhost:8080/api/v1'),
  apiTimeout: getNumberEnvVar('API_TIMEOUT', 30000),
  appName: getEnvVar('APP_NAME', 'Shopping List'),
  appEnv: getEnvVar('APP_ENV', 'development') as EnvConfig['appEnv'],
  enableMockApi: getBooleanEnvVar('ENABLE_MOCK_API', true),
  enableDebugLogs: getBooleanEnvVar('ENABLE_DEBUG_LOGS', false),
  googleClientId: getEnvVar(
    'GOOGLE_CLIENT_ID',
    '702911695224-9iv91ihjngqfjh761kd12gncul3sq89u.apps.googleusercontent.com'
  ),
};

export const isDevelopment = env.appEnv === 'development';
export const isProduction = env.appEnv === 'production';
export const isStaging = env.appEnv === 'staging';

// Log de configuração em desenvolvimento
if (isDevelopment && env.enableDebugLogs) {
  console.log('Environment Configuration:', {
    apiUrl: env.apiUrl,
    appEnv: env.appEnv,
    enableMockApi: env.enableMockApi,
  });
}
