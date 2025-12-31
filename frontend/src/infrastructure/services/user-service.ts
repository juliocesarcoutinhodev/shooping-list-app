/**
 * User Service
 * Serviço para operações de usuário
 */

import { User } from '@/src/domain/entities';
import { AuthRepository } from '@/src/domain/repositories';

export class UserService {
  constructor(private readonly repository: AuthRepository) {}

  /**
   * Busca dados do usuário autenticado
   * GET /api/v1/users/me
   */
  async getMe(): Promise<User> {
    return this.repository.getCurrentUser();
  }
}
