/**
 * Auth Service
 * Serviço de autenticação que coordena repository e storage
 */

import { AuthSession, User } from '@/src/domain/entities';
import { AuthRepository } from '@/src/domain/repositories';

import { getApiClient } from '../http/apiClient';
import { AuthStorageService } from '../storage';

export class AuthService {
  constructor(
    private readonly repository: AuthRepository,
    private readonly storage: AuthStorageService
  ) {}

  async login(email: string, password: string): Promise<AuthSession> {
    const session = await this.repository.login(email, password);
    await this.storage.saveSession(session);
    return session;
  }

  async register(name: string, email: string, password: string): Promise<AuthSession> {
    const session = await this.repository.register(name, email, password);
    await this.storage.saveSession(session);
    return session;
  }

  async loginWithGoogle(idToken: string): Promise<AuthSession> {
    const session = await this.repository.loginWithGoogle(idToken);
    await this.storage.saveSession(session);
    return session;
  }

  async logout(): Promise<void> {
    const refreshToken = await this.storage.getRefreshToken();

    if (refreshToken) {
      try {
        await this.repository.logout(refreshToken);
      } catch (_error) {
        // Ignoro erro ao fazer logout no backend
      }
    }

    await this.storage.clearSession();
    getApiClient().removeAuthToken();
  }

  async restoreSession(): Promise<User | null> {
    const accessToken = await this.storage.getAccessToken();
    const user = await this.storage.getUser();

    // Se não tenho token ou dados do usuário salvos, não há sessão para restaurar
    if (!accessToken || !user) {
      console.log('[AuthService] Nenhuma sessão salva encontrada');
      return null;
    }

    // Configuro o token no cliente HTTP para usar nas próximas requisições
    getApiClient().setAuthToken(accessToken);

    try {
      // Valido o access token fazendo uma chamada ao backend
      // Se o token for válido, recebo os dados atualizados do usuário
      console.log('[AuthService] Validando access token existente');
      return await this.repository.getCurrentUser();
    } catch (_error) {
      // Access token inválido ou expirado, vou tentar renovar usando refresh token
      console.log('[AuthService] Access token inválido, tentando refresh');
      const refreshToken = await this.storage.getRefreshToken();

      if (!refreshToken) {
        console.log('[AuthService] Nenhum refresh token disponível, limpando sessão');
        await this.storage.clearSession();
        return null;
      }

      try {
        // Tento renovar a sessão usando o refresh token
        const newSession = await this.repository.refreshToken(refreshToken);
        await this.storage.saveSession(newSession);
        console.log('[AuthService] Sessão renovada com sucesso via refresh token');
        return newSession.user;
      } catch (_refreshError) {
        // Refresh token também inválido, não há como recuperar a sessão
        console.log('[AuthService] Refresh token inválido, limpando sessão');
        await this.storage.clearSession();
        return null;
      }
    }
  }

  async refreshToken(): Promise<AuthSession | null> {
    const refreshToken = await this.storage.getRefreshToken();

    if (!refreshToken) {
      return null;
    }

    try {
      const session = await this.repository.refreshToken(refreshToken);
      await this.storage.saveSession(session);
      return session;
    } catch (_error) {
      await this.storage.clearSession();
      throw _error;
    }
  }
}
