/**
 * User Service Instance
 * Instância singleton configurada com dependências
 */

import { AuthApiDataSource } from '@/src/data/data-sources';
import { AuthRepositoryImpl } from '@/src/data/repositories';

import { getApiClient } from '../http/apiClient';

import { UserService } from './user-service';

// Crio instância única do serviço de usuário (reutiliza authRepository)
const authDataSource = new AuthApiDataSource(getApiClient());
const authRepository = new AuthRepositoryImpl(authDataSource);
export const userService = new UserService(authRepository);
