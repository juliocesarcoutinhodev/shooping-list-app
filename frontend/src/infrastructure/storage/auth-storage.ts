/**
 * Auth Storage Service
 * Gerencia persistência de tokens de autenticação
 */

import AsyncStorage from '@react-native-async-storage/async-storage';

import { AuthSession } from '@/src/domain/entities';

const KEYS = {
  ACCESS_TOKEN: '@shopping-list:access_token',
  REFRESH_TOKEN: '@shopping-list:refresh_token',
  USER: '@shopping-list:user',
} as const;

export interface AuthStorageService {
  saveSession(session: AuthSession): Promise<void>;
  getAccessToken(): Promise<string | null>;
  getRefreshToken(): Promise<string | null>;
  getUser(): Promise<AuthSession['user'] | null>;
  clearSession(): Promise<void>;
}

export class AuthAsyncStorage implements AuthStorageService {
  async saveSession(session: AuthSession): Promise<void> {
    try {
      await Promise.all([
        AsyncStorage.setItem(KEYS.ACCESS_TOKEN, session.accessToken),
        AsyncStorage.setItem(KEYS.REFRESH_TOKEN, session.refreshToken),
        AsyncStorage.setItem(KEYS.USER, JSON.stringify(session.user)),
      ]);
    } catch (error) {
      console.error('Erro ao salvar sessão:', error);
      throw error;
    }
  }

  async getAccessToken(): Promise<string | null> {
    try {
      return await AsyncStorage.getItem(KEYS.ACCESS_TOKEN);
    } catch (error) {
      console.error('Erro ao buscar access token:', error);
      return null;
    }
  }

  async getRefreshToken(): Promise<string | null> {
    try {
      return await AsyncStorage.getItem(KEYS.REFRESH_TOKEN);
    } catch (error) {
      console.error('Erro ao buscar refresh token:', error);
      return null;
    }
  }

  async getUser(): Promise<AuthSession['user'] | null> {
    try {
      const userJson = await AsyncStorage.getItem(KEYS.USER);
      return userJson ? JSON.parse(userJson) : null;
    } catch (error) {
      console.error('Erro ao buscar usuário:', error);
      return null;
    }
  }

  async clearSession(): Promise<void> {
    try {
      await AsyncStorage.multiRemove([KEYS.ACCESS_TOKEN, KEYS.REFRESH_TOKEN, KEYS.USER]);
    } catch (error) {
      console.error('Erro ao limpar sessão:', error);
      throw error;
    }
  }
}

export const authStorage = new AuthAsyncStorage();
