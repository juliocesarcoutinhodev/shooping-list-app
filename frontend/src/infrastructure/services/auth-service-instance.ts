/**
 * Auth Service Instance
 * Instância singleton configurada com dependências
 */

import { AuthApiDataSource } from '@/src/data/data-sources';
import { AuthRepositoryImpl } from '@/src/data/repositories';

import { getApiClient } from '../http/apiClient';
import { authStorage } from '../storage/auth-storage';

import { AuthService } from './auth-service';

// Crio instância única do serviço de autenticação
const authDataSource = new AuthApiDataSource(getApiClient());
const authRepository = new AuthRepositoryImpl(authDataSource);
export const authService = new AuthService(authRepository, authStorage);
