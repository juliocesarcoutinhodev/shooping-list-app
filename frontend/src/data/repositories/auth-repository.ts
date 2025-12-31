/**
 * Auth Repository Implementation
 * Implementa o contrato AuthRepository definido no domínio
 */

import { AuthSession, User } from '@/src/domain/entities';
import { AuthRepository } from '@/src/domain/repositories';
import { getApiClient } from '@/src/infrastructure/http/apiClient';

import { AuthDataSource } from '../data-sources';

export class AuthRepositoryImpl implements AuthRepository {
  constructor(private readonly dataSource: AuthDataSource) {}

  async login(email: string, password: string): Promise<AuthSession> {
    const response = await this.dataSource.login({ email, password });

    // Define token no apiClient para requisições futuras
    getApiClient().setAuthToken(response.accessToken);

    // Busco dados do usuário após login
    const userResponse = await this.dataSource.getCurrentUser();

    const user: User = {
      id: userResponse.id.toString(),
      email: userResponse.email,
      name: userResponse.name,
      provider: userResponse.provider as 'LOCAL' | 'GOOGLE',
      status: userResponse.status as 'ACTIVE' | 'INACTIVE',
      createdAt: userResponse.createdAt,
      updatedAt: userResponse.updatedAt,
    };

    return {
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      expiresIn: response.expiresIn,
      user,
    };
  }

  async register(name: string, email: string, password: string): Promise<AuthSession> {
    await this.dataSource.register({ name, email, password });

    // Após registro, faço login automaticamente
    return this.login(email, password);
  }

  async loginWithGoogle(idToken: string): Promise<AuthSession> {
    const response = await this.dataSource.loginWithGoogle({ idToken });

    // Define token no apiClient para requisições futuras
    getApiClient().setAuthToken(response.accessToken);

    // Busco dados do usuário após login
    const userResponse = await this.dataSource.getCurrentUser();

    const user: User = {
      id: userResponse.id.toString(),
      email: userResponse.email,
      name: userResponse.name,
      provider: userResponse.provider as 'LOCAL' | 'GOOGLE',
      status: userResponse.status as 'ACTIVE' | 'INACTIVE',
      createdAt: userResponse.createdAt,
      updatedAt: userResponse.updatedAt,
    };

    return {
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      expiresIn: response.expiresIn,
      user,
    };
  }

  async logout(refreshToken: string): Promise<void> {
    try {
      await this.dataSource.logout({ refreshToken });
    } finally {
      // Remove token mesmo se a requisição falhar
      getApiClient().removeAuthToken();
    }
  }

  async refreshToken(refreshToken: string): Promise<AuthSession> {
    const response = await this.dataSource.refreshToken({ refreshToken });

    // Atualizo token no apiClient
    getApiClient().setAuthToken(response.accessToken);

    // Busco dados do usuário atualizado
    const userResponse = await this.dataSource.getCurrentUser();

    const user: User = {
      id: userResponse.id.toString(),
      email: userResponse.email,
      name: userResponse.name,
      provider: userResponse.provider as 'LOCAL' | 'GOOGLE',
      status: userResponse.status as 'ACTIVE' | 'INACTIVE',
      createdAt: userResponse.createdAt,
      updatedAt: userResponse.updatedAt,
    };

    return {
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      expiresIn: response.expiresIn,
      user,
    };
  }

  async getCurrentUser(): Promise<User> {
    const response = await this.dataSource.getCurrentUser();

    return {
      id: response.id.toString(),
      email: response.email,
      name: response.name,
      provider: response.provider as 'LOCAL' | 'GOOGLE',
      status: response.status as 'ACTIVE' | 'INACTIVE',
      createdAt: response.createdAt,
      updatedAt: response.updatedAt,
    };
  }
}
