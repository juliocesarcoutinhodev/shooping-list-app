/**
 * Auth Data Source
 * Comunicação direta com a API de autenticação
 */

import type { HttpClient } from '@/src/infrastructure/http/apiClient';

import {
  LoginRequestDto,
  LoginResponseDto,
  LogoutRequestDto,
  RefreshTokenRequestDto,
  RefreshTokenResponseDto,
  RegisterRequestDto,
  RegisterResponseDto,
  UserMeResponseDto,
  GoogleLoginRequestDto,
  GoogleLoginResponseDto,
} from '../models';

export interface AuthDataSource {
  login(request: LoginRequestDto): Promise<LoginResponseDto>;
  register(request: RegisterRequestDto): Promise<RegisterResponseDto>;
  loginWithGoogle(request: GoogleLoginRequestDto): Promise<GoogleLoginResponseDto>;
  logout(request?: LogoutRequestDto): Promise<void>;
  refreshToken(request?: RefreshTokenRequestDto): Promise<RefreshTokenResponseDto>;
  getCurrentUser(): Promise<UserMeResponseDto>;
}

const API_V1_PREFIX = '/api/v1';

export class AuthApiDataSource implements AuthDataSource {
  constructor(private readonly httpClient: HttpClient) {}

  async login(request: LoginRequestDto): Promise<LoginResponseDto> {
    return this.httpClient.post<LoginResponseDto>(`${API_V1_PREFIX}/auth/login`, request);
  }

  async register(request: RegisterRequestDto): Promise<RegisterResponseDto> {
    return this.httpClient.post<RegisterResponseDto>(`${API_V1_PREFIX}/auth/register`, request);
  }

  async loginWithGoogle(request: GoogleLoginRequestDto): Promise<GoogleLoginResponseDto> {
    return this.httpClient.post<GoogleLoginResponseDto>(`${API_V1_PREFIX}/auth/google`, request);
  }

  async logout(request?: LogoutRequestDto): Promise<void> {
    return this.httpClient.post<void>(`${API_V1_PREFIX}/auth/logout`, request ?? {});
  }

  async refreshToken(request?: RefreshTokenRequestDto): Promise<RefreshTokenResponseDto> {
    return this.httpClient.post<RefreshTokenResponseDto>(`${API_V1_PREFIX}/auth/refresh`, request ?? {});
  }

  async getCurrentUser(): Promise<UserMeResponseDto> {
    return this.httpClient.get<UserMeResponseDto>(`${API_V1_PREFIX}/users/me`);
  }
}
